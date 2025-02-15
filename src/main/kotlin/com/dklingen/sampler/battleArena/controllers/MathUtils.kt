package com.dklingen.sampler.battleArena.controllers

import kotlin.math.pow
import kotlin.math.round

fun roundToDecimalPlaces(number: Double, places: Int): Double {
    val factor = 10.0.pow(places)
    return round(number * factor) / factor
}