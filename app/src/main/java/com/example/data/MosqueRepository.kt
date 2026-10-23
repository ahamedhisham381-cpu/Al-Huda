package com.example.data

import com.example.model.MosqueItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.*

/**
 * Finds real mosques near a given coordinate using the Overpass API
 * (OpenStreetMap's query engine) — free, no API key, community-maintained map
 * data. Results depend on how well a given area is mapped on OpenStreetMap.
 */
class MosqueRepository {

    // Public Overpass mirrors, tried in order in case one is overloaded.
    private val endpoints = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter"
    )

    suspend fun findNearby(lat: Double, lng: Double, radiusMeters: Int = 12000): List<MosqueItem> =
        withContext(Dispatchers.IO) {
            val query = """
                [out:json][timeout:15];
                (
                  node["amenity"="place_of_worship"]["religion"="muslim"](around:$radiusMeters,$lat,$lng);
                  way["amenity"="place_of_worship"]["religion"="muslim"](around:$radiusMeters,$lat,$lng);
                  relation["amenity"="place_of_worship"]["religion"="muslim"](around:$radiusMeters,$lat,$lng);
                );
                out center 30;
            """.trimIndent()

            for (endpoint in endpoints) {
                val body = httpPost(endpoint, query)
                if (body != null) {
                    val parsed = parse(body, lat, lng)
                    if (parsed.isNotEmpty()) return@withContext parsed.sortedBy { it.distanceKm }
                }
            }
            emptyList()
        }

    private fun httpPost(endpoint: String, query: String): String? = try {
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 20000
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("User-Agent", "AlHuda-Android/1.0")
        val payload = "data=" + URLEncoder.encode(query, "UTF-8")
        conn.outputStream.use { it.write(payload.toByteArray()) }
        try {
            if (conn.responseCode == 200) conn.inputStream.bufferedReader().use { it.readText() } else null
        } finally {
            conn.disconnect()
        }
    } catch (_: Exception) {
        null
    }

    private fun parse(body: String, originLat: Double, originLng: Double): List<MosqueItem> {
        return try {
            val elements = JSONObject(body).optJSONArray("elements") ?: return emptyList()
            val out = ArrayList<MosqueItem>()
            for (i in 0 until elements.length()) {
                val el = elements.getJSONObject(i)
                val tags = el.optJSONObject("tags") ?: JSONObject()
                val name = tags.optString("name").ifBlank {
                    tags.optString("name:en").ifBlank { "Mosque" }
                }

                // Nodes have lat/lon directly; ways/relations use the computed "center".
                val lat = if (el.has("lat")) el.getDouble("lat") else el.optJSONObject("center")?.optDouble("lat") ?: continue
                val lng = if (el.has("lon")) el.getDouble("lon") else el.optJSONObject("center")?.optDouble("lon") ?: continue

                val street = tags.optString("addr:street")
                val houseNum = tags.optString("addr:housenumber")
                val city = tags.optString("addr:city")
                val address = listOf(
                    listOf(houseNum, street).filter { it.isNotBlank() }.joinToString(" "),
                    city
                ).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Address not mapped yet" }

                val contact = tags.optString("phone").ifBlank { tags.optString("contact:phone") }
                    .ifBlank { tags.optString("website").ifBlank { tags.optString("opening_hours") } }
                    .ifBlank { "No contact info available" }

                val distanceKm = haversineKm(originLat, originLng, lat, lng)
                val bearing = bearingDegrees(originLat, originLng, lat, lng)

                out.add(
                    MosqueItem(
                        id = el.optString("id", el.optLong("id").toString()).ifBlank { "${lat}_$lng" },
                        name = name,
                        address = address,
                        distanceKm = (distanceKm * 10).roundToInt() / 10.0,
                        bearingDegrees = bearing,
                        contact = contact
                    )
                )
            }
            out
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun bearingDegrees(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dLng = Math.toRadians(lng2 - lng1)
        val y = sin(dLng) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLng)
        return ((Math.toDegrees(atan2(y, x)) + 360) % 360).toFloat()
    }
}
