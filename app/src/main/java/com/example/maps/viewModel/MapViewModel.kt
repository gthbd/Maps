package com.example.maps.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maps.data.GeoapifyApi
import com.example.maps.distanceMeters
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import org.maplibre.spatialk.geojson.Position
import retrofit2.HttpException

class MapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var routeJob: Job? = null

    fun onMapClick(point: Position) {
        _uiState.update { state ->
            if (state.phase == MeasurePhase.Result) return@update state
            state.copy(pendingPoint = point)
        }
    }

    fun onMeasureTypeChange(type: MeasureType) {
        _uiState.update { state ->
            if (state.phase == MeasurePhase.Result) return@update state
            state.copy(measureType = type)
        }
    }

    fun onConfirm() {
        val state = _uiState.value
        val pending = state.pendingPoint ?: return
        when (state.phase) {
            MeasurePhase.SelectingFirst -> _uiState.value = state.copy(
                phase = MeasurePhase.SelectingSecond,
                firstPoint = pending,
                pendingPoint = null,
            )
            MeasurePhase.SelectingSecond -> confirmSecondPoint(state, pending)
            MeasurePhase.Result -> Unit
        }
    }

    private fun confirmSecondPoint(state: MapUiState, second: Position) {
        val first = state.firstPoint ?: return
        val isRoute = state.measureType == MeasureType.Route
        _uiState.value = state.copy(
            phase = MeasurePhase.Result,
            secondPoint = second,
            pendingPoint = null,
            distanceMeters = distanceMeters(first, second),
            routeState = if (isRoute) RouteState.Loading else RouteState.Idle,
        )
        if (isRoute) requestRoute(first, second)
    }

    private fun requestRoute(from: Position, to: Position) {
        routeJob?.cancel()
        routeJob = viewModelScope.launch {
            val result: RouteState = try {
                val route = GeoapifyApi.getRoute(from, to).features.firstOrNull()
                if (route == null) {
                    RouteState.Error("Không tìm thấy đường đi giữa hai điểm")
                } else {
                    RouteState.Success(
                        distanceMeters = route.properties.distance,
                        durationSeconds = route.properties.time,
                        geometryGeoJson = route.geometry.toString(),
                    )
                }
            } catch (e: IOException) {
                RouteState.Error("Không kết nối được mạng")
            } catch (e: HttpException) {
                RouteState.Error("Máy chủ từ chối yêu cầu (mã ${e.code()})")
            } catch (e: SerializationException) {
                RouteState.Error("Dữ liệu trả về không đọc được")
            }
            _uiState.update { it.copy(routeState = result) }
        }
    }

    fun onRetry() {
        val state = _uiState.value
        if (state.routeState !is RouteState.Error) return
        val first = state.firstPoint ?: return
        val second = state.secondPoint ?: return
        _uiState.value = state.copy(routeState = RouteState.Loading)
        requestRoute(first, second)
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
        routeJob?.cancel()
        _uiState.value = MapUiState(measureType = _uiState.value.measureType)
    }
}