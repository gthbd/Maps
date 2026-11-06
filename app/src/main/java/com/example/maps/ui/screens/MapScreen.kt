package com.example.maps.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.maps.MapConfig
import com.example.maps.viewModel.MapViewModel
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.util.Locale

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        MapContent(
            onMapClick = viewModel::onMapClick,
            modifier = Modifier.weight(2f).fillMaxWidth(),
        )
        InfoPanel(
            selectedPoint = uiState.selectedPoint,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
    }
}

@Composable
private fun MapContent(onMapClick: (LatLng) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    // The listener is registered once, so it must read the latest callback instead of the first one
    val currentOnMapClick by rememberUpdatedState(onMapClick)

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                map.setStyle(Style.Builder().fromUri(MapConfig.STYLE_URL))
                map.cameraPosition = CameraPosition.Builder()
                    .target(MapConfig.HANOI_CENTER)
                    .zoom(MapConfig.INITIAL_ZOOM)
                    .build()
                map.addOnMapClickListener { point ->
                    currentOnMapClick(point)
                    true
                }
            }
        }
    }

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            // Destroy here, not via ON_DESTROY: composition may be disposed before that event is delivered
            mapView.onDestroy()
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier.fillMaxSize())
}

@Composable
private fun InfoPanel(selectedPoint: LatLng?, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, tonalElevation = 3.dp) {
        Text(
            text = selectedPoint?.let(::formatCoordinates) ?: "Chạm vào bản đồ để chọn địa điểm",
            modifier = Modifier.padding(16.dp),
        )
    }
}

// Locale.US keeps the decimal separator a dot regardless of device language
private fun formatCoordinates(point: LatLng): String =
    String.format(Locale.US, "%.5f, %.5f", point.latitude, point.longitude)