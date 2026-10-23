package com.example.data

import android.content.Context
import android.location.Geocoder
import com.example.model.GeoPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * Online place services (all free, HTTPS, no API key):
 *  - city search:      Open-Meteo Geocoding
 *  - reverse geocode:  BigDataCloud (client endpoint), then Android Geocoder
 *  - IP location:      ipwho.is, then ipapi.co (approximate, needs no permission)
 */
class GeoRepository(private val context: Context) {

    private fun httpGet(url: String, timeoutMs: Int = 7000): String? {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "AlHuda-Android/1.0")
            conn.setRequestProperty("Accept", "application/json")
            try {
                if (conn.responseCode == 200) conn.inputStream.bufferedReader().use { it.readText() } else null
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun apiLang(lang: String): String = when (lang) {
        "ar", "fr", "ur", "id" -> lang
        else -> "en"
    }

    private fun joinName(vararg parts: String?): String =
        parts.filterNotNull().map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString(", ")

    suspend fun searchCities(query: String, lang: String): List<GeoPlace> = withContext(Dispatchers.IO) {
        val q = URLEncoder.encode(query.trim(), "UTF-8")
        val body = httpGet(
            "https://geocoding-api.open-meteo.com/v1/search?name=$q&count=6&language=${apiLang(lang)}&format=json"
        ) ?: return@withContext emptyList()
        try {
            val arr = JSONObject(body).optJSONArray("results")
            val out = mutableListOf<GeoPlace>()
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    out += GeoPlace(
                        name = joinName(o.optString("name"), o.optString("admin1"), o.optString("country")),
                        latitude = o.getDouble("latitude"),
                        longitude = o.getDouble("longitude"),
                        countryCode = o.optString("country_code").uppercase(Locale.ROOT)
                    )
                }
            }
            out
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** City-level location from the device's public IP. Works with no GPS and no permission. */
    suspend fun ipLocate(): GeoPlace? = withContext(Dispatchers.IO) {
        httpGet("https://ipwho.is/")?.let { body ->
            try {
                val o = JSONObject(body)
                if (o.optBoolean("success", false)) {
                    return@withContext GeoPlace(
                        name = joinName(o.optString("city"), o.optString("region"), o.optString("country")),
                        latitude = o.getDouble("latitude"),
                        longitude = o.getDouble("longitude"),
                        countryCode = o.optString("country_code").uppercase(Locale.ROOT)
                    )
                }
            } catch (_: Exception) {}
        }
        httpGet("https://ipapi.co/json/")?.let { body ->
            try {
                val o = JSONObject(body)
                if (o.has("latitude") && !o.isNull("latitude")) {
                    return@withContext GeoPlace(
                        name = joinName(o.optString("city"), o.optString("region"), o.optString("country_name")),
                        latitude = o.getDouble("latitude"),
                        longitude = o.getDouble("longitude"),
                        countryCode = o.optString("country_code").uppercase(Locale.ROOT)
                    )
                }
            } catch (_: Exception) {}
        }
        null
    }

    /** Turns coordinates into a readable place name + country code. Null if every source fails. */
    suspend fun reverse(lat: Double, lng: Double, lang: String): GeoPlace? = withContext(Dispatchers.IO) {
        val la = String.format(Locale.US, "%.5f", lat)
        val lo = String.format(Locale.US, "%.5f", lng)

        httpGet(
            "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$la&longitude=$lo&localityLanguage=${apiLang(lang)}"
        )?.let { body ->
            try {
                val o = JSONObject(body)
                val name = joinName(
                    o.optString("city").ifBlank { o.optString("locality") },
                    o.optString("principalSubdivision"),
                    o.optString("countryName")
                )
                if (name.isNotBlank()) {
                    return@withContext GeoPlace(name, lat, lng, o.optString("countryCode").uppercase(Locale.ROOT))
                }
            } catch (_: Exception) {}
        }

        try {
            if (Geocoder.isPresent()) {
                @Suppress("DEPRECATION")
                val a = Geocoder(context, if (lang == "ar") Locale("ar") else Locale.US)
                    .getFromLocation(lat, lng, 1)?.firstOrNull()
                if (a != null) {
                    val name = joinName(a.locality ?: a.subAdminArea, a.adminArea, a.countryName)
                    if (name.isNotBlank()) {
                        return@withContext GeoPlace(name, lat, lng, (a.countryCode ?: "").uppercase(Locale.ROOT))
                    }
                }
            }
        } catch (_: Exception) {}
        null
    }
}
