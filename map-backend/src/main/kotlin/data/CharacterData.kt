package com.apxeolog.hnh.mapbackend.data

/**
 * @author APXEOLOG (Artyom Melnikov), at 04.02.2019
 */

/**
 * Request from the game client
 */
data class CharacterDataRequest(val name: String, val id: Long, val x: Int, val y: Int, val type: String = "detected")

/**
 * Internal character data
 */
data class CharacterData(val name: String, var id: Long, var position: Coordinate, var type: String) {
    var updated: Long = System.currentTimeMillis()

    constructor(request: CharacterDataRequest)
            : this(request.name, request.id, Coordinate(request.x, request.y), request.type)

    fun update(request: CharacterDataRequest) {
        updated = System.currentTimeMillis()
        id = request.id
        position = Coordinate(request.x, request.y)
        type = request.type
    }

    fun toResponse(): CharacterDataResponse {
        return CharacterDataResponse(name, id, position, type)
    }
}

/**
 * Response to the map
 */
data class CharacterDataResponse(val name: String, val id: Long, val position: Coordinate, val type: String)