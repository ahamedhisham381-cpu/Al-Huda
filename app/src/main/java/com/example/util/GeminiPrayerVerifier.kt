package com.example.util

import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Sends the "proof" photo to Gemini (Gemini Developer API, REST) and asks it to judge
 * whether the image plausibly shows evidence of prayer (a mat/rug laid out, a prayer
 * space, someone in prayer posture, etc). This is a lightweight honor-system check —
 * it is deliberately lenient, since the goal is to catch obviously unrelated photos
 * (a wall, a pet, a screenshot), not to police how someone prays.
 */
object GeminiPrayerVerifier {

    private const val MODEL = "gemini-2.5-flash"
    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    sealed class Result {
        /** Gemini thinks the photo plausibly shows evidence of prayer. */
        data class Approved(val reason: String) : Result()
        /** Gemini thinks the photo does NOT show evidence of prayer. */
        data class Rejected(val reason: String) : Result()
        /** Couldn't get a verdict (no key, no network, bad response, etc). */
        data class TechnicalError(val message: String) : Result()
    }

    private fun apiKey(): String? {
        val key = BuildConfig.GEMINI_API_KEY
        return key.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
    }

    suspend fun verify(photoFile: File, prayerName: String): Result = withContext(Dispatchers.IO) {
        val key = apiKey() ?: return@withContext Result.TechnicalError("Missing GEMINI_API_KEY")

        try {
            val imageBytes = photoFile.readBytes()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val prompt = """
                You are a lenient honor-system checker inside a Muslim prayer app. The user just
                took this photo right after claiming they prayed $prayerName, to confirm it to
                their own phone (no human ever sees this photo).

                Look at the photo and decide if it's a REASONABLE prayer-proof photo: for example
                a prayer mat/rug laid on the floor, a prayer room/corner, a mosque interior, prayer
                beads, or a person in prayer posture or prayer clothing. It does NOT need to show
                someone mid-prostration — a mat laid out right after prayer is enough. Be generous:
                the goal is only to catch photos that are obviously unrelated to prayer (e.g. a
                selfie with no prayer context, a random object, a blank wall/ceiling, a screenshot,
                food, a pet with no prayer setting). When in doubt, approve.

                Respond ONLY as JSON matching the schema.
            """.trimIndent()

            val schema = JSONObject().apply {
                put("type", "OBJECT")
                put(
                    "properties",
                    JSONObject().apply {
                        put("prayed_evidence", JSONObject().put("type", "BOOLEAN"))
                        put("reason", JSONObject().put("type", "STRING"))
                    }
                )
                put("required", listOf("prayed_evidence", "reason"))
            }

            val body = JSONObject().apply {
                put(
                    "contents",
                    listOf(
                        JSONObject().apply {
                            put(
                                "parts",
                                listOf(
                                    JSONObject().put("text", prompt),
                                    JSONObject().put(
                                        "inline_data",
                                        JSONObject().apply {
                                            put("mime_type", "image/jpeg")
                                            put("data", base64Image)
                                        }
                                    )
                                )
                            )
                        }
                    )
                )
                put(
                    "generationConfig",
                    JSONObject().apply {
                        put("temperature", 0)
                        put("responseMimeType", "application/json")
                        put("responseSchema", schema)
                    }
                )
            }

            val request = Request.Builder()
                .url("$ENDPOINT?key=$key")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.TechnicalError(
                        "Gemini request failed: ${response.code}"
                    )
                }

                val candidateText = JSONObject(responseText)
                    .optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (candidateText.isNullOrBlank()) {
                    return@withContext Result.TechnicalError("Empty Gemini response")
                }

                val verdict = JSONObject(candidateText)
                val prayed = verdict.optBoolean("prayed_evidence", true)
                val reason = verdict.optString("reason", "")

                if (prayed) Result.Approved(reason) else Result.Rejected(reason)
            }
        } catch (e: Exception) {
            Result.TechnicalError(e.message ?: "Unknown error")
        }
    }
}
