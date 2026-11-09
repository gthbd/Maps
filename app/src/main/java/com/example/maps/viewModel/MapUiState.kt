package com.example.maps.viewModel

import org.maplibre.spatialk.geojson.Position

enum class MeasurePhase { SelectingFirst, SelectingSecond, Result }

data class MapUiState(
    val phase: MeasurePhase = MeasurePhase.SelectingFirst,
    val firstPoint: Position? = null,
    val pendingPoint: Position? = null,
    val secondPoint: Position? = null,
    val distanceMeters: Double? = null,
) {
    val markerPoints: List<Position>
        get() = listOfNotNull(firstPoint, pendingPoint, secondPoint)
}