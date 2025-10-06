package com.example.aprimortech.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.math.*

object RouteDistanceCalculator {

    /**
     * Calcula a distância via Google Directions API
     * Se falhar, usa cálculo Haversine como fallback
     */
    suspend fun calculateRouteDistanceKm(
        apiKey: String,
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): Double = withContext(Dispatchers.IO) {
        return@withContext try {
            android.util.Log.d("RouteDistanceCalculator", "=== CALCULANDO DISTÂNCIA VIA GOOGLE MAPS ===")
            android.util.Log.d("RouteDistanceCalculator", "Origem: $originLat, $originLng")
            android.util.Log.d("RouteDistanceCalculator", "Destino: $destLat, $destLng")

            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=$originLat,$originLng&" +
                    "destination=$destLat,$destLng&" +
                    "key=$apiKey&" +
                    "mode=driving&" +
                    "avoid=tolls" // Evita pedágios para dar opção manual

            val conn = URL(url).openConnection() as HttpsURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)

            if (json.getString("status") == "OK") {
                val routes = json.getJSONArray("routes")
                if (routes.length() > 0) {
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    var totalMeters = 0L

                    for (i in 0 until legs.length()) {
                        val distance = legs.getJSONObject(i).getJSONObject("distance")
                        totalMeters += distance.getInt("value")
                    }

                    val distanceKm = totalMeters / 1000.0
                    android.util.Log.d("RouteDistanceCalculator", "Distância calculada via API: $distanceKm km")
                    distanceKm
                } else {
                    android.util.Log.w("RouteDistanceCalculator", "Nenhuma rota encontrada, usando Haversine")
                    calculateHaversineDistance(originLat, originLng, destLat, destLng)
                }
            } else {
                android.util.Log.w("RouteDistanceCalculator", "Status da API: ${json.getString("status")}, usando Haversine")
                calculateHaversineDistance(originLat, originLng, destLat, destLng)
            }
        } catch (e: Exception) {
            android.util.Log.e("RouteDistanceCalculator", "Erro na API do Google Maps, usando Haversine", e)
            calculateHaversineDistance(originLat, originLng, destLat, destLng)
        }
    }

    /**
     * Cálculo de distância usando fórmula Haversine (linha reta)
     * Usado como fallback quando a API do Google Maps falha
     */
    private fun calculateHaversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        android.util.Log.d("RouteDistanceCalculator", "Calculando distância via Haversine")

        val R = 6371.0 // Raio da Terra em km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = R * c

        android.util.Log.d("RouteDistanceCalculator", "Distância Haversine: $distance km")
        return distance
    }
}
