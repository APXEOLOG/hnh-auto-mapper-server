package com.apxeolog.hnh.mapbackend

import com.apxeolog.hnh.mapbackend.data.Coordinate
import com.apxeolog.hnh.mapbackend.data.MarkerData
import com.apxeolog.hnh.mapbackend.data.MarkerDataRequest
import com.apxeolog.hnh.mapbackend.services.GridData
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.sql.Connection

/**
 * @author APXEOLOG (Artyom Melnikov), at 28.01.2019
 */

object DBO

object Grids : Table() {
    val id = long("id").primaryKey().uniqueIndex()
    val x = integer("x")
    val y = integer("y")
    val updated = datetime("updated")
}

object Zooms : Table() {
    val zoom = integer("zoom").primaryKey(0)
    val x = integer("x").primaryKey(1)
    val y = integer("y").primaryKey(2)
}

object Markers : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val gridId = long("grid_id")
    val x = integer("x")
    val y = integer("y")
    val name = varchar("name", 32)
    val image = varchar("image", 64)

    init {
        index(true, gridId, x, y)
    }
}

fun insertMarker(markerData: MarkerDataRequest): Int? {
    synchronized(DBO) {
        return try {
            transaction {
                Markers.insert {
                    it[gridId] = markerData.gridId
                    it[x] = markerData.x
                    it[y] = markerData.y
                    it[name] = markerData.name
                    it[image] = markerData.image
                }[Markers.id]
            }
        } catch (ex: ExposedSQLException) {
            null
        }
    }
}

fun getAllGrids(map: MutableMap<Long, GridData>) {
    synchronized(DBO) {
        transaction {
            Grids.selectAll()
                .map { it[Grids.id] to GridData(it[Grids.id], Coordinate(it[Grids.x], it[Grids.y]),
                    it[Grids.updated].millis) }
                .toMap(map)
        }
    }
}

fun getAllMarkers(map: MutableMap<Int, MarkerData>) {
    synchronized(DBO) {
        transaction {
            Markers.selectAll()
                .map { it[Markers.id] to
                        MarkerData(it[Markers.id], it[Markers.name], it[Markers.gridId],
                            Coordinate(it[Markers.x], it[Markers.y]), it[Markers.image]) }
                .toMap(map)
        }
    }
}

fun markZoomGeneration(baseCoordinate: Coordinate, zoomLevel: Int) {
    synchronized(DBO) {
        val zoomed = baseCoordinate.zoom(zoomLevel)
        try {
            transaction {
                Zooms.insert {
                    it[zoom] = zoomLevel
                    it[x] = zoomed.x
                    it[y] = zoomed.y
                }
            }
        } catch (sqlException: ExposedSQLException) {
            // No fallbacks
        }
    }
}

fun insertOrUpdateGrid(gridData: GridData) {
    synchronized(DBO) {
        try {
            transaction {
                Grids.insert {
                    it[id] = gridData.id
                    it[x] = gridData.coordinate.x
                    it[y] = gridData.coordinate.y
                    it[updated] = DateTime.now()
                }
            }
        } catch (sqlException: ExposedSQLException) {
            transaction {
                Grids.update({ Grids.id eq gridData.id }) {
                    it[updated] = DateTime.now()
                }
            }
        }
        markZoomGeneration(gridData.coordinate, 1)
    }
}

fun deleteGridFromDB(id: Long) {
    synchronized(DBO) {
        try {
            transaction {
                Grids.deleteWhere { Grids.id eq id }
            }
        } catch (sqlException: ExposedSQLException) { }
    }
}

fun cleanupDB() {
    transaction {
        SchemaUtils.drop(Grids, Zooms, Markers)
        SchemaUtils.createMissingTablesAndColumns(Grids, Zooms, Markers)
    }
}

fun getNextZoomToGenerate(zoomLevel: Int) : Coordinate? {
    synchronized(DBO) {
        return transaction {
            val coordinate = Zooms.select { Zooms.zoom eq zoomLevel }.limit(1)
                .map { Coordinate(it[Zooms.x], it[Zooms.y]) }
                .firstOrNull()
            if (coordinate != null) {
                Zooms.deleteWhere { (Zooms.zoom eq zoomLevel) and (Zooms.x eq coordinate.x) and (Zooms.y eq coordinate.y) }
            }
            coordinate
        }
    }
}

fun setupDatabaseConnection() {
    Database.connect("jdbc:sqlite:mapdata.db", driver = "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    TransactionManager.manager.defaultRepetitionAttempts = 0
    transaction {
        SchemaUtils.createMissingTablesAndColumns(Grids, Zooms, Markers)
    }
}