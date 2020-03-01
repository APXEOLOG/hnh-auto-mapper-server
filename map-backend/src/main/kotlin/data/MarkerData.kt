package com.apxeolog.hnh.mapbackend.data

/**
 * @author APXEOLOG (Artyom Melnikov), at 04.02.2019
 */

/**
 * Request from the game client
 */
data class MarkerDataRequest(val name: String, val gridId: Long, val x: Int, val y: Int, val image: String = "custom")

/**
 * Internal marker data
 */
data class MarkerData(val id: Int, val name: String, val gridId: Long, val offset: Coordinate, val image: String) {
    lateinit var gridCoordinates: Coordinate

    constructor(id: Int, request: MarkerDataRequest) : this(id, request.name, request.gridId,
        Coordinate(request.x, request.y), request.image)

    fun toResponse(): MarkerDataResponse {
        return MarkerDataResponse(id, name, gridCoordinates * 100 + offset, image)
    }
}

/**
 * Response to the map
 */
data class MarkerDataResponse(val id: Int, val name: String, val position: Coordinate, val image: String)
