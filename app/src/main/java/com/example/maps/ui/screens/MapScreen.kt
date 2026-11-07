package com.example.maps.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.maps.MapConfig
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.style.BaseStyle

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        MapContent(modifier = Modifier.weight(2f).fillMaxWidth())
        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            tonalElevation = 3.dp,
        ) {}
    }
}

@Composable
private fun MapContent(modifier: Modifier = Modifier) {
    val mapState = rememberMapState(
        baseStyle = BaseStyle.Uri(MapConfig.STYLE_URL),
        initialCameraPosition = CameraPosition(
            center = MapConfig.HANOI_CENTER,
            zoom = MapConfig.INITIAL_ZOOM,
        ),
    )

    MaplibreMap(state = mapState, modifier = modifier.fillMaxSize())
}