package com.example.maps

import android.location.Location
import org.maplibre.spatialk.geojson.Position
import java.util.Locale
import kotlin.math.roundToInt

fun distanceMeters(from: Position, to: Position): Double {
    val result = FloatArray(1)
    Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, result)
    return result[0].toDouble()
}

fun formatDistance(meters: Double): String {
    // Round first, then pick the unit: 999.6 m must read "1.00 km", not "1000 m"
    val roundedMeters = meters.roundToInt()
    return if (roundedMeters < 1000) {
        "$roundedMeters m"
    } else {
        String.format(Locale.ROOT, "%.2f km", roundedMeters / 1000.0)
    }
}