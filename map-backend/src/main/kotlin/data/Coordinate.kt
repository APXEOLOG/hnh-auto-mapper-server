package com.apxeolog.hnh.mapbackend.data

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * @author APXEOLOG (Artyom Melnikov), at 04.02.2019
 */

data class Coordinate(val x: Int, val y: Int) {

    operator fun minus(other: Coordinate) : Coordinate {
        return Coordinate(x - other.x, y - other.y)
    }

    operator fun plus(other: Coordinate) : Coordinate {
        return Coordinate(x + other.x, y + other.y)
    }

    operator fun div(other: Int) : Coordinate {
        return Coordinate(x  / other, y / other)
    }

    operator fun times(other: Int) : Coordinate {
        return Coordinate(x  * other, y * other)
    }

    fun zoom(zoom: Int) : Coordinate {
        val factor = Math.pow(2.0, zoom.toDouble()).toInt()
        return Coordinate((x / factor) * factor, (y / factor) * factor)
    }

    @JsonIgnore
    fun getGridName() : String {
        return "${this.x}_${this.y}.png"
    }
}