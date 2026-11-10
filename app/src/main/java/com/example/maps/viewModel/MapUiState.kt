package com.example.maps.viewModel

import org.maplibre.spatialk.geojson.Position

enum class MeasurePhase { SelectingFirst, SelectingSecond, Result }
enum class MeasureType { StraightLine, Route }

data class MapUiState(
    val phase: MeasurePhase = MeasurePhase.SelectingFirst,
    val measureType: MeasureType = MeasureType.StraightLine,
    val firstPoint: Position? = null,
    val pendingPoint: Position? = null,
    val secondPoint: Position? = null,
    val distanceMeters: Double? = null,
    val routeState: RouteState = RouteState.Idle,
) {
    val markerPoints: List<Position>
        get() = listOfNotNull(firstPoint, pendingPoint, secondPoint)
}

sealed interface RouteState {
    data object Idle : RouteState
    data object Loading : RouteState
    data class Success(
        val distanceMeters: Double,
        val durationSeconds: Double,
        val geometryGeoJson: String,
    ) : RouteState
    data class Error(val message: String) : RouteState
}