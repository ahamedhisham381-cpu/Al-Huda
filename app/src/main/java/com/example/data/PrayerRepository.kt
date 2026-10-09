package com.example.data

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

class PrayerRepository {

    val defaultCity = CityLocation("Makkah, Saudi Arabia", 21.4225, 39.8262)

    val presetCities = listOf(
        CityLocation("Makkah, Saudi Arabia", 21.4225, 39.8262),
        CityLocation("Madinah, Saudi Arabia", 24.4672, 39.6068),
        CityLocation("Cairo, Egypt", 30.0444, 31.2357),
        CityLocation("Istanbul, Turkey", 41.0082, 28.9784),
        CityLocation("Dubai, UAE", 25.2048, 55.2708),
        CityLocation("London, UK", 51.5074, -0.1278),
        CityLocation("New York, USA", 40.7128, -74.0060),
        CityLocation("Toronto, Canada", 43.6532, -79.3832),
        CityLocation("Jakarta, Indonesia", -6.2088, 106.8456),
        CityLocation("Kuala Lumpur, Malaysia", 3.1390, 101.6869),
        CityLocation("Paris, France", 48.8566, 2.3522),
        CityLocation("Tokyo, Japan", 35.6762, 139.6503)
    )

    fun searchCity(query: String): CityLocation? {
        val q = query.trim().lowercase(Locale.ROOT)
        return presetCities.firstOrNull { it.name.lowercase(Locale.ROOT).contains(q) }
            ?: presetCities.firstOrNull { q.contains(it.name.split(",")[0].lowercase(Locale.ROOT)) }
    }

    suspend fun getPrayerTimesOnline(lat: Double, lng: Double): Pair<PrayerTimes, HijriDate>? {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = System.currentTimeMillis() / 1000
                val urlString = "https://api.aladhan.com/v1/timings/$timestamp?latitude=$lat&longitude=$lng&method=2"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 6000
                conn.readTimeout = 6000
                conn.requestMethod = "GET"

                if (conn.responseCode == 200) {
                    val stream = conn.inputStream
                    val responseText = stream.bufferedReader().use { it.readText() }
                    conn.disconnect()

                    val json = JSONObject(responseText)
                    if (json.getInt("code") == 200) {
                        val dataObj = json.getJSONObject("data")
                        val timingsObj = dataObj.getJSONObject("timings")
                        val dateObj = dataObj.getJSONObject("date")
                        val hijriObj = dateObj.getJSONObject("hijri")
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
                            day = hijriObj.getString("day"),
                            monthAr = monthObj.optString("ar", "رمضان"),
                            monthEn = monthObj.optString("en", "Ramadan"),
                            year = hijriObj.getString("year")
                        )

                        return@withContext Pair(times, hijri)
                    }
                }
                conn.disconnect()
                null
            } catch (e: Exception) {
                null
            }
        }
    }

    fun calculateOfflinePrayerTimes(
        lat: Double,
        lng: Double,
        calendar: Calendar = Calendar.getInstance()
    ): Pair<PrayerTimes, HijriDate> {
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val timeZone = calendar.timeZone.rawOffset / (1000.0 * 3600.0)

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
        val asrAltitude = Math.toDegrees(atan(1.0 / (1.0 + tan(Math.toRadians(abs(lat - declination))))))
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
