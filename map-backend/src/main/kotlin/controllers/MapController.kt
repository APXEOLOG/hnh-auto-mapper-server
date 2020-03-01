package com.apxeolog.hnh.mapbackend.controllers

import com.apxeolog.hnh.mapbackend.data.CharacterDataRequest
import com.apxeolog.hnh.mapbackend.data.CharacterDataResponse
import com.apxeolog.hnh.mapbackend.data.MarkerDataRequest
import com.apxeolog.hnh.mapbackend.data.MarkerDataResponse
import com.apxeolog.hnh.mapbackend.services.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * @author APXEOLOG (Artyom Melnikov), at 21.01.2019
 */

@Controller
class MapController(private val mapService: MapService) {

    @Value("\${map.admin.token}")
    private val adminToken: String? = null

    /**
     * Receive grid image from the client
     */
    @PostMapping("api/v2/updateGrid")
    fun uploadMinimap(@RequestParam("file") gridImage: MultipartFile,
                      @RequestParam("id") gridId: Long,
                      @RequestParam("x") x: Int,
                      @RequestParam("y") y: Int): ResponseEntity<String> {
        mapService.updateGrid(gridImage, gridId, x, y)
        return ResponseEntity(HttpStatus.OK)
    }

    /**
     * Receive character position update
     */
    @PostMapping("api/v2/updateCharacter")
    fun updateCharacterPosition(@RequestBody data: CharacterDataRequest): ResponseEntity<String> {
        mapService.updateCharacterData(data)
        return ResponseEntity(HttpStatus.OK)
    }

    /**
     * Receive markers update
     */
    @PostMapping("api/v1/uploadMarkers")
    fun receiveMarkerData(@RequestBody markers: List<MarkerDataRequest>): ResponseEntity<String> {
        mapService.updateMarkersData(markers)
        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping("api/v1/removeGrid")
    fun deleteGrid(@RequestParam x: Int,
                   @RequestParam y: Int,
                   @RequestParam token: String): ResponseEntity<String> {
        return if (adminToken == token) {
            mapService.deleteGrid(x, y)
            ResponseEntity(HttpStatus.OK)
        } else ResponseEntity(HttpStatus.BAD_REQUEST)
    }

    @GetMapping("api/v1/setZeroGrid")
    fun setZeroGrid(@RequestParam gridId: Long,
                   @RequestParam token: String): ResponseEntity<String> {
        return if (adminToken == token) {
            mapService.setZeroGrid(gridId)
            ResponseEntity(HttpStatus.OK)
        } else ResponseEntity(HttpStatus.BAD_REQUEST)
    }

    @GetMapping("api/v1/cleanup")
    fun cleanup(@RequestParam token: String): ResponseEntity<String> {
        return if (adminToken == token) {
            mapService.cleanup()
            ResponseEntity(HttpStatus.OK)
        } else ResponseEntity(HttpStatus.BAD_REQUEST)
    }

    /**
     * Client requested geolocation
     */
    @GetMapping("api/v1/locate")
    fun locate(gridId: Long): ResponseEntity<String> {
        val coordinate = mapService.locate(gridId)
        return if (coordinate != null) {
            ResponseEntity.ok("${coordinate.x};${coordinate.y}")
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @CrossOrigin("*")
    @GetMapping("api/v1/characters")
    fun getCharacters(): ResponseEntity<List<CharacterDataResponse>> {
        return ResponseEntity.ok(mapService.getCharacters())
    }

    @CrossOrigin("*")
    @GetMapping("api/v1/markers")
    fun getMarkers(): ResponseEntity<List<MarkerDataResponse>> {
        return ResponseEntity.ok(mapService.getMarkers())
    }
}
