package com.example.maps

import org.maplibre.android.geometry.LatLng

object MapConfig {
    const val INITIAL_ZOOM = 12.0
    val HANOI_CENTER = LatLng(21.0285, 105.8542)
    val STYLE_URL =
        "https://maps.geoapify.com/v1/styles/osm-bright/style.json?apiKey=${BuildConfig.GEOAPIFY_API_KEY}"
}