package com.example.maps.data

import com.example.maps.BuildConfig
import com.example.maps.MapConfig.DEFAULT_MODE
import com.example.maps.MapConfig.DEFAULT_TYPE
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.maplibre.spatialk.geojson.Position
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

interface RoutingApi {
    @GET("v1/routing")
    suspend fun getRoute(
        @Query("waypoints") waypoints: String,
        @Query("mode") mode: String = DEFAULT_MODE,
        @Query("type") type: String = DEFAULT_TYPE,
        @Query("apiKey") apiKey: String = BuildConfig.GEOAPIFY_API_KEY,
    ): RoutingResponse
}

object GeoapifyApi {
    private fun Position.toWaypoint(): String = "lonlat:${longitude},${latitude}"
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val routingApi: RoutingApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.geoapify.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create()
    }
    suspend fun getRoute(from: Position, to: Position): RoutingResponse {
        val waypoints = "${from.toWaypoint()}|${to.toWaypoint()}"
        return routingApi.getRoute(waypoints = waypoints)
    }
}