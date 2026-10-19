package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProjectDto(
    @Json(name = "_id")
    val id: String? = null,
    val name: String,
    val location: String,
    val budget: Double,
    val status: String = "جارية",
    val startDate: String? = null
)
