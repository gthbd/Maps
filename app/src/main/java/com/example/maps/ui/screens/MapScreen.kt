package com.example.maps.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.maps.MapConfig
import com.example.maps.formatDistance
import com.example.maps.viewModel.MapUiState
import com.example.maps.viewModel.MapViewModel
import com.example.maps.viewModel.MeasurePhase
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import java.util.Locale
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        MapContent(
            uiState = uiState,
            onMapClick = viewModel::onMapClick,
            modifier = Modifier.weight(2f).fillMaxWidth(),
        )
        InfoPanel(
            uiState = uiState,
            onConfirm = viewModel::onConfirm,
            onBack = viewModel::onBack,
            onReset = viewModel::onReset,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
    }
}

@Composable
private fun MapContent(
    uiState: MapUiState,
    onMapClick: (Position) -> Unit,
    modifier: Modifier = Modifier,
) {
    // khởi tạo bản đồ
    val mapState = rememberMapState(
        baseStyle = BaseStyle.Uri(MapConfig.STYLE_URL),
        initialCameraPosition = CameraPosition(
            center = MapConfig.HANOI_CENTER,
            zoom = MapConfig.INITIAL_ZOOM,
        ),
    ) {
        val linePoints = when {
            uiState.firstPoint != null && uiState.secondPoint != null -> listOf(uiState.firstPoint, uiState.secondPoint)
            uiState.phase == MeasurePhase.SelectingSecond && uiState.firstPoint != null && uiState.pendingPoint != null -> listOf(uiState.firstPoint, uiState.pendingPoint)
            else -> null
        }
        if (linePoints != null) {
            val lineSource = rememberGeoJsonSource(GeoJsonData.JsonString(toLineGeoJson(linePoints[0], linePoints[1])))
            LineLayer(
                id = MapConfig.LINE_LAYER_ID,
                source = lineSource,
                color = const(Color(0xFF2B8DFB)),
                opacity = const(0.6f),
                width = const(4.dp),
                cap = const(LineCap.Round),
                join = const(LineJoin.Round),
            )
        }

        // create marker points
        val markerPoints = uiState.markerPoints
        if (markerPoints.isNotEmpty()) {
            val markerSource = rememberGeoJsonSource(GeoJsonData.JsonString(toPointsGeoJson(markerPoints)))
            CircleLayer(
                id = MapConfig.MARKER_LAYER_ID,
                source = markerSource,
                color = const(Color(0xFF2B8DFB)),
                radius = const(8.dp),
            )
        }
    }

    MaplibreMap(
        state = mapState,
        modifier = modifier.fillMaxSize(),
        interactions = MapInteractions {
            callbacks {
                click {
                    onEvent { event ->
                        event.position?.let(onMapClick)
                        ClickResult.Consume
                    }
                }
            }
        },
    )
}

@Composable
private fun InfoPanel(
    uiState: MapUiState,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, tonalElevation = 3.dp) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = titleFor(uiState.phase), style = MaterialTheme.typography.titleMedium)
            uiState.firstPoint?.let { Text("Điểm 1: ${formatCoordinates(it)}") }
            uiState.secondPoint?.let { Text("Điểm 2: ${formatCoordinates(it)}") }
            uiState.distanceMeters?.let {
                Text("Khoảng cách đường chim bay: ${formatDistance(it)}", style = MaterialTheme.typography.titleMedium)
            }
            if (uiState.phase != MeasurePhase.Result) {
                Text(pendingText(uiState))
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (uiState.phase) {
                    MeasurePhase.SelectingFirst -> {
                        Button(
                            onClick = onConfirm,
                            enabled = uiState.pendingPoint != null,
                            modifier = Modifier.weight(1f),
                        ) { Text("Xác nhận") }
                    }
                    MeasurePhase.SelectingSecond -> {
                        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Quay lại") }
                        Button(
                            onClick = onConfirm,
                            enabled = uiState.pendingPoint != null,
                            modifier = Modifier.weight(1f),
                        ) { Text("Xác nhận") }
                    }
                    MeasurePhase.Result -> {
                        Button(onClick = onReset, modifier = Modifier.weight(1f)) { Text("Đo lại") }
                    }
                }
            }
        }
    }
}

private fun titleFor(phase: MeasurePhase): String = when (phase) {
    MeasurePhase.SelectingFirst -> "Chọn điểm thứ nhất"
    MeasurePhase.SelectingSecond -> "Chọn điểm thứ hai"
    MeasurePhase.Result -> "Kết quả"
}

private fun pendingText(uiState: MapUiState): String {
    val pending = uiState.pendingPoint ?: return "Chạm vào bản đồ để chọn địa điểm"
    val label = if (uiState.phase == MeasurePhase.SelectingFirst) "Điểm 1" else "Điểm 2"
    return "$label (đang chọn): ${formatCoordinates(pending)}"
}

// Locale.ROOT keeps the decimal separator a dot regardless of device language
private fun formatCoordinates(point: Position): String =
    String.format(Locale.ROOT, "%.5f, %.5f", point.latitude, point.longitude)

// GeoJSON orders coordinates as [longitude, latitude], the reverse of how people write them
private fun toPointsGeoJson(points: List<Position>): String {
    val features = points.joinToString(",") {
        String.format(
            Locale.ROOT,
            """{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[%f,%f]}}""",
            it.longitude,
            it.latitude,
        )
    }
    return """{"type":"FeatureCollection","features":[$features]}"""
}

private fun toLineGeoJson(start: Position, end: Position): String {
    return String.format(
        Locale.ROOT,
        """{"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"LineString","coordinates":[[%f,%f],[%f,%f]]}}]}""",
        start.longitude,
        start.latitude,
        end.longitude,
        end.latitude,
    )
}
