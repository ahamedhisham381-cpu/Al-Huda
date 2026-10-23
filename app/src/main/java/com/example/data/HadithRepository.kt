package com.example.data

import android.content.Context
import com.example.model.HadithItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

data class HadithCollection(
    val slug: String,       // e.g. "bukhari" -> eng-bukhari / ara-bukhari
    val nameEn: String,
    val nameAr: String,
    val approxCount: Int    // used only to pick a random number for "Hadith of the Day"
)

/**
 * Real Hadith data from the fawazahmed0 Hadith API — a free, no-key, no-rate-limit
 * JSON API served over the jsDelivr CDN (https://github.com/fawazahmed0/hadith-api),
 * scraped from sunnah.com. Mirrors the same fetch-and-cache approach already used
 * for Quran text (alquran.cloud) and prayer times (Aladhan) elsewhere in this app.
 */
class HadithRepository(private val context: Context) {

    companion object {
        private const val BASE = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions"
    }

    val collections = listOf(
        HadithCollection("bukhari", "Sahih al-Bukhari", "صحيح البخاري", 7563),
        HadithCollection("muslim", "Sahih Muslim", "صحيح مسلم", 7470),
        HadithCollection("abudawud", "Sunan Abi Dawud", "سنن أبي داود", 5274),
        HadithCollection("tirmidhi", "Jami at-Tirmidhi", "جامع الترمذي", 3956),
        HadithCollection("nasai", "Sunan an-Nasa'i", "سنن النسائي", 5758),
        HadithCollection("ibnmajah", "Sunan Ibn Majah", "سنن ابن ماجه", 4341),
        HadithCollection("malik", "Muwatta Malik", "موطأ مالك", 1858),
        HadithCollection("nawawi40", "40 Hadith Nawawi", "الأربعون النووية", 42),
        HadithCollection("qudsi40", "40 Hadith Qudsi", "الأربعون القدسية", 40)
    )

    private fun httpGet(urlString: String): String? = try {
        val conn = URL(urlString).openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 15000
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "AlHuda-Android/1.0")
        try {
            if (conn.responseCode == 200) conn.inputStream.bufferedReader().use { it.readText() } else null
        } finally {
            conn.disconnect()
        }
    } catch (_: Exception) {
        null
    }

    private fun cacheFile(edition: String): File =
        File(File(context.filesDir, "hadith_cache").apply { mkdirs() }, "$edition.json")

    private fun parseHadithsFromEdition(body: String, collectionLabel: String): List<HadithItem> {
        return try {
            val root = JSONObject(body)
            val metadata = root.optJSONObject("metadata")
            val sections = metadata?.optJSONObject("section")
            val hadiths = root.optJSONArray("hadiths") ?: return emptyList()
            val out = ArrayList<HadithItem>(hadiths.length())
            for (i in 0 until hadiths.length()) {
                val h = hadiths.getJSONObject(i)
                val number = h.optInt("hadithnumber", i + 1)
                val bookRef = h.optJSONObject("reference")?.optInt("book", -1) ?: -1
                val chapterTitle = if (bookRef >= 0) sections?.optString(bookRef.toString(), "") ?: "" else ""
                val grade = h.optJSONArray("grades")?.optJSONObject(0)?.optString("grade", "") ?: ""
                out.add(
                    HadithItem(
                        id = number,
                        collection = collectionLabel,
                        number = "#$number",
                        narrator = "",
                        arabicText = "",
                        englishText = h.optString("text", ""),
                        chapter = chapterTitle,
                        grade = grade.ifBlank { "Reported" }
                    )
                )
            }
            out
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Downloads (or loads from a one-time local cache — this text never changes) every
     * hadith in a collection's English edition, with chapter titles resolved.
     */
    suspend fun getCollection(slug: String): List<HadithItem> = withContext(Dispatchers.IO) {
        val collection = collections.firstOrNull { it.slug == slug } ?: return@withContext emptyList()
        val file = cacheFile("eng-${slug}")
        val body = if (file.exists()) {
            file.readText()
        } else {
            val fetched = httpGet("$BASE/eng-$slug.min.json") ?: httpGet("$BASE/eng-$slug.json")
            if (fetched != null) try { file.writeText(fetched) } catch (_: Exception) {}
            fetched
        } ?: return@withContext emptyList()
        parseHadithsFromEdition(body, collection.nameEn)
    }

    /** Fetches the Arabic text for one hadith on demand (kept out of the bulk download above). */
    suspend fun getArabicText(slug: String, hadithNumber: Int): String? = withContext(Dispatchers.IO) {
        val body = httpGet("$BASE/ara-$slug/$hadithNumber.min.json")
            ?: httpGet("$BASE/ara-$slug/$hadithNumber.json")
            ?: return@withContext null
        try {
            JSONObject(body).optJSONArray("hadiths")?.optJSONObject(0)?.optString("text")?.ifBlank { null }
        } catch (_: Exception) {
            null
        }
    }

    /** One random hadith (with Arabic + English fetched together) for a "Hadith of the Day" card. */
    suspend fun getRandomHadith(): HadithItem? = coroutineScope {
        withContext(Dispatchers.IO) {
            repeat(3) {
                val collection = collections.filter { it.slug != "nawawi40" && it.slug != "qudsi40" }.random()
                val number = Random.nextInt(1, collection.approxCount)

                val englishDeferred = async { httpGet("$BASE/eng-${collection.slug}/$number.min.json") }
                val arabicDeferred = async { httpGet("$BASE/ara-${collection.slug}/$number.min.json") }
                val englishBody = englishDeferred.await()
                val arabicBody = arabicDeferred.await()

                if (englishBody != null) {
                    try {
                        val h = JSONObject(englishBody).optJSONArray("hadiths")?.optJSONObject(0)
                        val text = h?.optString("text")?.takeIf { it.isNotBlank() }
                        if (text != null) {
                            val grade = h.optJSONArray("grades")?.optJSONObject(0)?.optString("grade", "") ?: ""
                            val arabicText = arabicBody?.let {
                                try { JSONObject(it).optJSONArray("hadiths")?.optJSONObject(0)?.optString("text") } catch (_: Exception) { null }
                            } ?: ""
                            return@withContext HadithItem(
                                id = number,
                                collection = collection.nameEn,
                                number = "#$number",
                                narrator = "",
                                arabicText = arabicText.orEmpty(),
                                englishText = text,
                                chapter = "",
                                grade = grade.ifBlank { "Reported" }
                            )
                        }
                    } catch (_: Exception) { /* try another collection/number */ }
                }
            }
            null
        }
    }

    /** Local, in-memory search across an already-loaded collection (no extra network call). */
    fun search(items: List<HadithItem>, query: String): List<HadithItem> {
        if (query.isBlank()) return items
        val q = query.trim()
        val qNum = q.removePrefix("#").toIntOrNull()
        return items.filter {
            it.englishText.contains(q, ignoreCase = true) ||
                it.chapter.contains(q, ignoreCase = true) ||
                (qNum != null && it.id == qNum)
        }
    }
}
