package com.example.maps

import org.maplibre.spatialk.geojson.Position


object MapConfig {
    const val INITIAL_ZOOM = 12.0
    const val MARKER_LAYER_ID = "selected-point-layer"
    const val LINE_LAYER_ID = "distance-line-layer"
    val HANOI_CENTER = Position(latitude = 21.0285, longitude = 105.8542)
    val STYLE_URL =
        "https://maps.geoapify.com/v1/styles/osm-bright/style.json?apiKey=${BuildConfig.GEOAPIFY_API_KEY}"
    const val DEFAULT_MODE = "drive"
    const val DEFAULT_TYPE = "short"
}