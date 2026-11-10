package com.example.maps.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RoutingResponse(
    val features: List<RouteFeature>,
)

//tuyến đường cụ thể
@Serializable
data class RouteFeature(
    val properties: RouteProperties,
    val geometry: JsonElement,  //JsonElement: đại diện cho bất kỳ kiểu dữ liệu nào trong Kotlin
)

// thông tin về tuyến đường
@Serializable
data class RouteProperties(
    val distance: Double,
    val time: Double,
)