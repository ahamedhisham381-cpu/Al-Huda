package com.example.data

import android.content.SharedPreferences
import com.example.model.CityLocation
import com.example.model.HijriDate
import com.example.model.PrayerTimes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale
import kotlin.math.*

class PrayerRepository(private val cachePrefs: SharedPreferences? = null) {

    val defaultCity = CityLocation("Makkah, Saudi Arabia", 21.4225, 39.8262, "SA")

    val presetCities = listOf(
        CityLocation("Makkah, Saudi Arabia", 21.4225, 39.8262, "SA"),
        CityLocation("Madinah, Saudi Arabia", 24.4672, 39.6068, "SA"),
        CityLocation("Cairo, Egypt", 30.0444, 31.2357, "EG"),
        CityLocation("Istanbul, Turkey", 41.0082, 28.9784, "TR"),
        CityLocation("Dubai, UAE", 25.2048, 55.2708, "AE"),
        CityLocation("London, UK", 51.5074, -0.1278, "GB"),
        CityLocation("New York, USA", 40.7128, -74.0060, "US"),
        CityLocation("Toronto, Canada", 43.6532, -79.3832, "CA"),
        CityLocation("Jakarta, Indonesia", -6.2088, 106.8456, "ID"),
        CityLocation("Kuala Lumpur, Malaysia", 3.1390, 101.6869, "MY"),
        CityLocation("Paris, France", 48.8566, 2.3522, "FR"),
        CityLocation("Tokyo, Japan", 35.6762, 139.6503, "JP")
    )

    fun searchCity(query: String): CityLocation? {
        val q = query.trim().lowercase(Locale.ROOT)
        return presetCities.firstOrNull { it.name.lowercase(Locale.ROOT).contains(q) }
            ?: presetCities.firstOrNull { q.contains(it.name.split(",")[0].lowercase(Locale.ROOT)) }
    }

    /** Shown in Settings so the person can override the auto-picked method if they prefer another. */
    val manualMethods = listOf(
        3 to "Muslim World League",
        2 to "Islamic Society of North America (ISNA)",
        4 to "Umm al-Qura, Makkah",
        5 to "Egyptian General Authority",
        1 to "University of Islamic Sciences, Karachi",
        16 to "Dubai (UAE)",
        13 to "Diyanet (Turkey)",
        17 to "JAKIM (Malaysia)",
        20 to "Kemenag (Indonesia)",
        7 to "Institute of Geophysics, Tehran",
        15 to "Moonsighting Committee Worldwide"
    )

    /** Applies the person's manual per-prayer minute adjustments to a computed set of times. */
    fun applyOffsets(times: PrayerTimes, offsets: Map<String, Int>): PrayerTimes {
        fun shift(hhmm: String, minutes: Int): String {
            if (minutes == 0) return hhmm
            val parts = hhmm.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: return hhmm
            val m = parts.getOrNull(1)?.toIntOrNull() ?: return hhmm
            var total = (h * 60 + m + minutes) % (24 * 60)
            if (total < 0) total += 24 * 60
            return String.format(Locale.US, "%02d:%02d", total / 60, total % 60)
        }
        return times.copy(
            fajr = shift(times.fajr, offsets["Fajr"] ?: 0),
            dhuhr = shift(times.dhuhr, offsets["Dhuhr"] ?: 0),
            asr = shift(times.asr, offsets["Asr"] ?: 0),
            maghrib = shift(times.maghrib, offsets["Maghrib"] ?: 0),
            isha = shift(times.isha, offsets["Isha"] ?: 0)
        )
    }

    /** Best Aladhan calculation method for a country (falls back to Muslim World League). */
    fun methodFor(countryCode: String): Int = when (countryCode.uppercase(Locale.ROOT)) {
        "SA" -> 4                                   // Umm Al-Qura, Makkah
        "AE" -> 16                                  // Dubai
        "KW" -> 9
        "QA" -> 10
        "BH", "OM" -> 8                             // Gulf region
        "EG", "SD", "SY", "LB" -> 5                 // Egyptian General Authority
        "PK", "BD", "IN", "AF" -> 1                 // Karachi
        "IR" -> 7
        "TR" -> 13                                  // Diyanet
        "SG" -> 11
        "FR" -> 12
        "RU" -> 14
        "MY" -> 17                                  // JAKIM
        "TN" -> 18
        "DZ" -> 19
        "ID" -> 20                                  // Kemenag
        "MA" -> 21
        "PT" -> 22
        "JO" -> 23
        "US", "CA" -> 2                             // ISNA
        else -> 3                                   // Muslim World League
    }

    private fun httpGet(urlString: String): String? {
        return try {
            val conn = URL(urlString).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 10000
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
    }

    private fun httpGetWithRetry(url: String): String? = httpGet(url) ?: httpGet(url)

    private fun toResult(entry: JSONObject): Pair<PrayerTimes, HijriDate> {
        val timingsObj = entry.getJSONObject("timings")
        val hijriObj = entry.getJSONObject("date").getJSONObject("hijri")
        val monthObj = hijriObj.getJSONObject("month")
        val times = PrayerTimes(
            fajr = cleanTime(timingsObj.getString("Fajr")),
            sunrise = cleanTime(timingsObj.getString("Sunrise")),
            dhuhr = cleanTime(timingsObj.getString("Dhuhr")),
            asr = cleanTime(timingsObj.getString("Asr")),
            maghrib = cleanTime(timingsObj.getString("Maghrib")),
            isha = cleanTime(timingsObj.getString("Isha"))
        )
        val hijri = HijriDate(
            day = hijriObj.getString("day").trimStart('0').ifEmpty { "1" },
            monthAr = monthObj.optString("ar", ""),
            monthEn = monthObj.optString("en", ""),
            year = hijriObj.getString("year")
        )
        return Pair(times, hijri)
    }

    private fun parseCalendarDay(body: String, day: Int): Pair<PrayerTimes, HijriDate>? {
        return try {
            val json = JSONObject(body)
            if (json.optInt("code") != 200) return null
            val arr = json.getJSONArray("data")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val d = o.getJSONObject("date").getJSONObject("gregorian").getString("day").toIntOrNull()
                if (d == day) return toResult(o)
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Prayer times from the Aladhan API (https://aladhan.com/prayer-times-api).
     * Downloads the whole month once and caches it, so later days / app restarts work offline.
     * Returns null only if there is no cache and the network is unreachable.
     */
    suspend fun getPrayerTimesOnline(
        lat: Double,
        lng: Double,
        countryCode: String = "",
        hanafi: Boolean = false,
        methodOverride: Int? = null
    ): Pair<PrayerTimes, HijriDate>? {
        return withContext(Dispatchers.IO) {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val day = cal.get(Calendar.DAY_OF_MONTH)

            val method = methodOverride ?: methodFor(countryCode)
            val school = if (hanafi) 1 else 0
            val params = String.format(
                Locale.US,
                "latitude=%.5f&longitude=%.5f&method=%d&school=%d%s",
                lat, lng, method, school,
                if (abs(lat) >= 48.0) "&latitudeAdjustmentMethod=3" else ""
            )

            val cacheKey = String.format(Locale.US, "cal|%d-%02d|%.2f|%.2f|%d|%d", year, month, lat, lng, method, school)

            // 1. Cached month
            cachePrefs?.getString(cacheKey, null)?.let { cached ->
                parseCalendarDay(cached, day)?.let { return@withContext it }
            }

            // 2. Aladhan monthly calendar (then cache it)
            val calBody = httpGetWithRetry("https://api.aladhan.com/v1/calendar/$year/$month?$params")
            if (calBody != null) {
                val parsed = parseCalendarDay(calBody, day)
                if (parsed != null) {
                    val prefs = cachePrefs
                    if (prefs != null) {
                        val editor = prefs.edit()
                        prefs.all.keys.filter { it.startsWith("cal|") }.forEach { editor.remove(it) }
                        editor.putString(cacheKey, calBody).apply()
                    }
                    return@withContext parsed
                }
            }

            // 3. Aladhan single-day endpoint
            val dateStr = String.format(Locale.US, "%02d-%02d-%04d", day, month, year)
            val dayBody = httpGetWithRetry("https://api.aladhan.com/v1/timings/$dateStr?$params")
            if (dayBody != null) {
                try {
                    val json = JSONObject(dayBody)
                    if (json.optInt("code") == 200) return@withContext toResult(json.getJSONObject("data"))
                } catch (_: Exception) {}
            }
            null
        }
    }

    fun calculateOfflinePrayerTimes(
        lat: Double,
        lng: Double,
        calendar: Calendar = Calendar.getInstance(),
        hanafi: Boolean = false
    ): Pair<PrayerTimes, HijriDate> {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        // DST-aware offset for the given date
        val timeZone = calendar.timeZone.getOffset(calendar.timeInMillis) / (1000.0 * 3600.0)

        // Approximate solar calculations
        val b = 2 * PI * (dayOfYear - 81) / 365.0
        val eot = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b) // Equation of time in minutes
        val declination = Math.toDegrees(asin(sin(Math.toRadians(23.45)) * sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81))))) // in degrees

        val solarNoonMinutes = 720.0 - (4.0 * lng) - eot + (timeZone * 60.0)

        val latRad = Math.toRadians(lat)
        val decRad = Math.toRadians(declination)

        fun hourAngle(angle: Double): Double {
            val cosHA = (sin(Math.toRadians(angle)) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
            val clamped = cosHA.coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(clamped)) * 4.0 // minutes
        }

        val fajrHA = hourAngle(-18.0)
        val sunriseHA = hourAngle(-0.833)
        val ishaHA = hourAngle(-17.0)

        // Asr Shafii (shadow length = 1 + noon shadow)
        val noonAngle = 90.0 - lat + declination
        val shadowFactor = if (hanafi) 2.0 else 1.0
        val asrAltitude = Math.toDegrees(atan(1.0 / (shadowFactor + tan(Math.toRadians(abs(lat - declination))))))
        val asrHA = hourAngle(asrAltitude)

        val fajrMinutes = (solarNoonMinutes - fajrHA).toInt()
        val sunriseMinutes = (solarNoonMinutes - sunriseHA).toInt()
        val dhuhrMinutes = solarNoonMinutes.toInt()
        val asrMinutes = (solarNoonMinutes + asrHA).toInt()
        val maghribMinutes = (solarNoonMinutes + sunriseHA).toInt()
        val ishaMinutes = (solarNoonMinutes + ishaHA).toInt()

        fun formatMin(min: Int): String {
            val normalized = (min % 1440 + 1440) % 1440
            val h = normalized / 60
            val m = normalized % 60
            return String.format(Locale.US, "%02d:%02d", h, m)
        }

        val prayerTimes = PrayerTimes(
            fajr = formatMin(fajrMinutes),
            sunrise = formatMin(sunriseMinutes),
            dhuhr = formatMin(dhuhrMinutes),
            asr = formatMin(asrMinutes),
            maghrib = formatMin(maghribMinutes),
            isha = formatMin(ishaMinutes)
        )

        // Estimate current Hijri date (approximated based on Islamic epoch)
        val hijriDate = calculateApproximateHijri(calendar)

        return Pair(prayerTimes, hijriDate)
    }

    private fun calculateApproximateHijri(cal: Calendar): HijriDate {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)

        // Julian Day Number
        var a = (14 - m) / 12
        var y1 = y + 4800 - a
        var m1 = m + 12 * a - 3
        var jdn = d + (153 * m1 + 2) / 5 + 365 * y1 + y1 / 4 - y1 / 100 + y1 / 400 - 32045

        // Approximate Islamic date from JDN
        val l = jdn - 1948440 + 10632
        val n = (l - 1) / 10631
        val l2 = l - 10631 * n + 354
        val j = ((10985 - l2) / 5316) * ((50 * l2) / 17719) + (l2 / 5670) * ((43 * l2) / 15238)
        val l3 = l2 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val hijriMonth = (24 * l3) / 709
        val hijriDay = l3 - (709 * hijriMonth) / 24
        val hijriYear = 30 * n + j - 30

        val monthNamesEn = listOf(
            "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
            "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )

        val monthNamesAr = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )

        val safeMonthIndex = (hijriMonth - 1).coerceIn(0, 11)

        return HijriDate(
            day = hijriDay.toString(),
            monthAr = monthNamesAr[safeMonthIndex],
            monthEn = monthNamesEn[safeMonthIndex],
            year = hijriYear.toString()
        )
    }

    private fun cleanTime(raw: String): String {
        return raw.split(" ")[0].trim()
    }

    fun formatTo12Hr(time24: String): String {
        if (time24.isBlank()) return ""
        val parts = time24.split(":")
        if (parts.size < 2) return time24
        val hours = parts[0].toIntOrNull() ?: return time24
        val minutes = parts[1].toIntOrNull() ?: return time24
        val period = if (hours >= 12) "PM" else "AM"
        val hours12 = when {
            hours == 0 -> 12
            hours > 12 -> hours - 12
            else -> hours
        }
        return String.format(Locale.US, "%d:%02d %s", hours12, minutes, period)
    }

    fun generateAnnualScheduleText(locationName: String, lat: Double, lng: Double, year: Int): String {
        val sb = StringBuilder()
        sb.appendLine("============================================================")
        sb.appendLine("           AL-HUDA ANNUAL PRAYER SCHEDULE - $year")
        sb.appendLine("============================================================")
        sb.appendLine("Location: $locationName ($lat° N, $lng° E)")
        sb.appendLine("Generated on: ${java.util.Date()}")
        sb.appendLine("Note: Times are in 12-hour AM/PM format.")
        sb.appendLine("------------------------------------------------------------\n")

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.DAY_OF_YEAR, 1)

        val maxDays = if (cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365) 366 else 365

        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_YEAR, day)
            val dateStr = String.format(
                Locale.US,
                "%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )

            val (times, hijri) = calculateOfflinePrayerTimes(lat, lng, cal)

            sb.appendLine(
                "$dateStr (${hijri.day} ${hijri.monthEn}) | Fajr: ${formatTo12Hr(times.fajr)} | " +
                        "Dhuhr: ${formatTo12Hr(times.dhuhr)} | Asr: ${formatTo12Hr(times.asr)} | " +
                        "Maghrib: ${formatTo12Hr(times.maghrib)} | Isha: ${formatTo12Hr(times.isha)}"
            )
        }

        return sb.toString()
    }
}
