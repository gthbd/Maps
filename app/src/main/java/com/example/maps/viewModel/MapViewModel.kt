package com.example.maps.viewModel

import androidx.lifecycle.ViewModel
import com.example.maps.distanceMeters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.maplibre.spatialk.geojson.Position

class MapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun onMapClick(point: Position) {
        _uiState.update { state ->
            if (state.phase == MeasurePhase.Result) return@update state
            state.copy(pendingPoint = point)
        }
    }

    fun onConfirm() {
        _uiState.update { state ->
            val pending = state.pendingPoint ?: return@update state
            when (state.phase) {
                MeasurePhase.SelectingFirst -> state.copy(
                    phase = MeasurePhase.SelectingSecond,
                    firstPoint = pending,
                    pendingPoint = null,
                )
                MeasurePhase.SelectingSecond -> {
                    val first = state.firstPoint ?: return@update state
                    state.copy(
                        phase = MeasurePhase.Result,
                        secondPoint = pending,
                        pendingPoint = null,
                        distanceMeters = distanceMeters(first, pending),
                    )
                }
                MeasurePhase.Result -> state
            }
        }
    }

    fun onBack() {
        _uiState.update { state ->
            if (state.phase != MeasurePhase.SelectingSecond) return@update state
            state.copy(
                phase = MeasurePhase.SelectingFirst,
                firstPoint = null,
                pendingPoint = state.firstPoint,
            )
        }
    }

    fun onReset() {
        _uiState.value = MapUiState()
    }
}