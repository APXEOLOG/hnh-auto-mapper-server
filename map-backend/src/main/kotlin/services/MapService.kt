package com.apxeolog.hnh.mapbackend.services

import com.apxeolog.hnh.mapbackend.*
import com.apxeolog.hnh.mapbackend.data.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

/**
 * @author APXEOLOG (Artyom Melnikov), at 21.01.2019
 */

const val MAX_ZOOM: Int = 5

@Service
class MapService(meterRegistry: MeterRegistry) {
    private val unscaledGrids: File = mapdataFolder.resolve("0")
    private val pngOptimizer: File = mapdataFolder.resolve("pngquant.exe")

    private val characters: ConcurrentHashMap<String, CharacterData> = ConcurrentHashMap()
    private val cachedGrids: MutableMap<Long, GridData> = ConcurrentHashMap()
    private val cachedMarkers: MutableMap<Int, MarkerData> = ConcurrentHashMap()

    init {
        // Zero-zoom folder
        if (!unscaledGrids.exists()) unscaledGrids.mkdir()

        // Setup local DB connection
        setupDatabaseConnection()

        // Fetch cache
        getAllGrids(cachedGrids)
        getAllMarkers(cachedMarkers)
        cachedMarkers.values.forEach { markerData ->
            cachedGrids[markerData.gridId]?.coordinate?.let {
                markerData.gridCoordinates = it
            }
        }
        println("Loaded ${cachedGrids.size} grids, ${cachedMarkers.size} markers")

        // Extract optimizer on Windows
        if (System.getProperty("os.name").contains("Windows")) {
            if (!pngOptimizer.exists()) {
                MapService::class.java.classLoader.getResourceAsStream("pngquant.exe").use { input ->
                    Files.copy(input, pngOptimizer.toPath())
                }
            }
        }

        meterRegistry.gaugeMapSize("cached.grids", Tags.empty(), cachedGrids)
        meterRegistry.gaugeMapSize("available.markers", Tags.empty(), cachedMarkers)
        meterRegistry.gaugeMapSize("players.online", Tags.empty(), characters)
    }

    fun locate(gridId: Long): Coordinate? {
        return cachedGrids[gridId]?.coordinate
    }

    fun updateGrid(gridImage: MultipartFile, gridId: Long, x: Int, y: Int) {
        val cachedData = cachedGrids[gridId]
        val coordinates = Coordinate(x, y)
        if (cachedData != null) {
            if (cachedData.coordinate != coordinates) {
                throw IllegalArgumentException("Grid '$gridId' already exists with different coordinates ${cachedData.coordinate}")
            } else {
                // All fine, check time, update duplicated grids every 30 minutes
                if (System.currentTimeMillis() - cachedData.updated > TimeUnit.MINUTES.toMillis(30)) {
                    cachedData.updated = System.currentTimeMillis()
                    actuallySaveImage(gridImage, gridId, coordinates)
                }
            }
        } else {
            cachedGrids[gridId] = GridData(gridId, coordinates)
            actuallySaveImage(gridImage, gridId, coordinates)
        }
    }

    fun actuallySaveImage(gridImage: MultipartFile, gridId: Long, coordinates: Coordinate) {
        val minimapFile = unscaledGrids.resolve(coordinates.getGridName())
        if (!minimapFile.exists()) minimapFile.createNewFile()
        gridImage.transferTo(minimapFile)
        optimizeImage(minimapFile)
        // Update DB
        insertOrUpdateGrid(GridData(gridId, coordinates))
    }

    /**
     * Update character's position
     */
    fun updateCharacterData(characterDataRequest: CharacterDataRequest) {
        val character = characters[characterDataRequest.name]
        if (character != null) {
            character.update(characterDataRequest)
        } else {
            characters[characterDataRequest.name] = CharacterData(characterDataRequest)
        }
    }

    /**
     * Update markers received from the game client
     */
    fun updateMarkersData(markers: List<MarkerDataRequest>) {
        var addedAmount = 0
        markers.forEach { markerDataRequest ->
            val gridData = cachedGrids[markerDataRequest.gridId]
            if (gridData != null) {
                val id = insertMarker(markerDataRequest)
                if (id != null) {
                    // Unique marker
                    val markerData = MarkerData(id, markerDataRequest)
                    markerData.gridCoordinates = gridData.coordinate
                    cachedMarkers[markerData.id] = markerData
                    addedAmount++
                }
            }
        }
        println("Added $addedAmount markers out of ${markers.size} received")
    }

    /**
     * Get all available markers
     */
    fun getMarkers(): List<MarkerDataResponse> {
        return cachedMarkers.values.map { it.toResponse() }
    }

    /**
     * Get all characters
     */
    fun getCharacters(): List<CharacterDataResponse> {
        return characters.values.map { it.toResponse() }
    }

    fun deleteGrid(x: Int, y: Int) {
        val coordinates = Coordinate(x, y)
        val grid = cachedGrids.values.find { it.coordinate == coordinates }
        if (grid != null) {
            deleteGridFromDB(grid.id)
            val minimapFile = unscaledGrids.resolve(grid.coordinate.getGridName())
            if (minimapFile.exists()) minimapFile.delete()
            cachedGrids.remove(grid.id)
        }
    }

    fun setZeroGrid(gridId: Long) {
        if (cachedGrids.isEmpty()) {
            val gridData = GridData(gridId, Coordinate(0, 0), 0)
            cachedGrids[gridId] = gridData
            updateGridIdsFile()
            insertOrUpdateGrid(gridData)
        }
    }

    fun cleanup() {
        cachedGrids.clear()
        cleanupDB()
        cleanupCharacters()
        updateGridIdsFile()
        if (mapdataFolder.deleteRecursively()) {
            mapdataFolder.mkdirs()
        }
    }

    @Scheduled(fixedDelay = 500)
    fun processUploadedSessions() {
        for (zoom in 1..MAX_ZOOM) {
            val coordinate = getNextZoomToGenerate(zoom)
            if (coordinate != null) {
                generateZoomImage(coordinate, zoom)
                // Add next zoom generation task
                markZoomGeneration(coordinate, zoom + 1)
                break
            }
        }
    }

    @Scheduled(fixedDelay = 1000 * 10)
    fun cleanupCharacters() {
        val currentTime = System.currentTimeMillis()
        characters.values
            .filter { (currentTime - it.updated) > TimeUnit.SECONDS.toMillis(10) }
            .forEach { characters.remove(it.name) }
    }

    @Scheduled(fixedDelay = 1000 * 60 * 15, initialDelay = 5000)
    fun updateGridIdsFile() {
        val coordinates = cachedGrids.values.map { "${it.id},${it.coordinate.x},${it.coordinate.y}" }
        Files.write(mapdataFolder.resolve("mapdata_index").toPath(), coordinates,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    private fun generateZoomImage(tl: Coordinate, zoom: Int) {
        val inputFolder = mapdataFolder.resolve((zoom - 1).toString())
        val outputFolder = mapdataFolder.resolve(zoom.toString())
        if (!outputFolder.exists()) outputFolder.mkdir()
        val outputFile = outputFolder.resolve(tl.getGridName())
        if (!outputFile.exists()) outputFile.createNewFile()

        val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
        val graphics = image.graphics as Graphics2D

        val factor = Math.pow(2.0, (zoom - 1).toDouble()).toInt()
        val coordinates = listOf(tl, tl + Coordinate(0, factor),
            tl + Coordinate(factor, 0), tl + Coordinate(factor, factor))

        coordinates.forEach {
            val offset = ((it - tl) * 50) / factor
            drawMinimapImage(graphics, inputFolder.resolve(it.getGridName()), offset)
        }
        ImageIO.write(image, "png", outputFile)
        optimizeImage(outputFile)
    }

    private fun drawMinimapImage(graphics2D: Graphics2D, imageFile: File, offset: Coordinate) {
        if (imageFile.exists()) {
            graphics2D.drawImage(ImageIO.read(imageFile), offset.x, offset.y, 50, 50, null)
        } else {
            graphics2D.color = Color.BLACK
            graphics2D.fillRect(offset.x, offset.y, 50, 50)
        }
    }

    private fun optimizeImage(file: File) {
        val pngquant =
            if (System.getProperty("os.name").contains("Windows")) pngOptimizer.toString() else "pngquant"
        ProcessBuilder(
            pngquant, "--force", "--skip-if-larger",
            "--output=$file", "--speed=1", "--quality=50-90", "--", "$file"
        )
            .directory(mapdataFolder)
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor()
    }
}


data class ClientCharacterData(val name: String, val characterId: Long, val position: Coordinate, val type: String)

class GridData(val id: Long, val coordinate: Coordinate, var updated: Long = System.currentTimeMillis()) {

    override fun toString(): String {
        return "$id($coordinate)"
    }
}

