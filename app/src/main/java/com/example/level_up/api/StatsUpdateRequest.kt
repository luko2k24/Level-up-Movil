package com.example.level_up.api

data class StatsUpdateRequest(
    val puntos: Int,
    val nivel: Int,
    val totalCompras: Int
)