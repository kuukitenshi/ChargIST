package cmu.group2.chargist.data.repository

import android.location.Location
import cmu.group2.chargist.data.database.AppDatabase
import cmu.group2.chargist.data.database.dao.ChargerDao
import cmu.group2.chargist.data.database.dao.ReviewDao
import cmu.group2.chargist.data.database.dao.StationDao
import cmu.group2.chargist.data.database.entity.toDomain
import cmu.group2.chargist.data.database.entity.toEntity
import cmu.group2.chargist.data.model.GeoLocation
import cmu.group2.chargist.data.model.Station
import cmu.group2.chargist.data.model.StationFormData
import cmu.group2.chargist.data.supabase.SupaTable
import cmu.group2.chargist.data.supabase.Supabase
import cmu.group2.chargist.data.supabase.dto.SupaCharger
import cmu.group2.chargist.data.supabase.dto.SupaStation
import cmu.group2.chargist.data.supabase.dto.toEntity
import cmu.group2.chargist.data.supabase.table
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object StationRepository {
    private val stationDao: StationDao by lazy { AppDatabase.getDatabase().stationDao() }
    private val chargerDao: ChargerDao by lazy { AppDatabase.getDatabase().chargerDao() }
    private val reviewDao: ReviewDao by lazy { AppDatabase.getDatabase().reviewDao() }

    val stations: Flow<List<Station>> = stationDao.getAllStationFullFlow().map { list ->
        list.map { stationFull ->
            stationFull.toDomain()
        }
    }

    fun getStationById(stationId: Long): Flow<Station?> {
        return stationDao.getStationFullByIdFlow(stationId).map { it?.toDomain() }
    }

    suspend fun getStationByIdInstance(stationId: Long): Station? {
        return stationDao.getStationFullById(stationId)?.toDomain()
    }

    suspend fun addStation(formData: StationFormData) {
        val request = SupaStation(
            name = formData.name,
            latitude = formData.location!!.latitude,
            longitude = formData.location!!.longitude,
            paymentMethods = formData.paymentMethods.joinToString(","),
            nearbyServices = if (formData.nearbyServices.isEmpty()) { null } else { formData.nearbyServices.joinToString(",") },
            imageUrl = formData.imageUri?.toString()
        )
        var supaStation = Supabase.table(SupaTable.Stations).insert(request) {
            select()
        }.decodeSingle<SupaStation>()
        formData.imageBytes?.let {
            val path = "stations/" + supaStation.id + ".bmp"
            val bucket = Supabase.storage.from("chargist")
            bucket.upload(path, it) {
                upsert = true
            }
            val url = bucket.publicUrl(path)
            supaStation = Supabase.table(SupaTable.Stations).update({
                set("imageUrl", url)
            }) {
                filter {
                    eq("id", supaStation.id!!)
                }
                select()
            }.decodeSingle<SupaStation>()
        }
        val chargersRequests = formData.bundles.flatMap { it.chargers }.map {
            SupaCharger(
                stationId = supaStation.id!!,
                type = it.type.name,
                power = it.power.name,
                price = it.price,
                status = it.status.name,
                issue = it.issue.name
            )
        }
        val chargers = Supabase.table(SupaTable.Chargers).insert(chargersRequests) {
            select()
        }.decodeList<SupaCharger>()
        stationDao.insert(supaStation.toEntity())
        chargers.forEach { chargerDao.insert(it.toEntity()) }
    }

    suspend fun updateStation(station: Station, formData: StationFormData) {
        Supabase.table(SupaTable.Chargers).delete {
            filter {
                eq("stationId", station.id)
            }
        }
        chargerDao.deleteByStation(station.id)

        var newImageUrl: String? = null
        formData.imageBytes?.let {
            val path = "stations/" + station.id + ".bmp"
            val bucket = Supabase.storage.from("chargist")
            bucket.upload(path, it) {
                upsert = true
            }
            newImageUrl = bucket.publicUrl(path)
        }

        val supaStation = Supabase.table(SupaTable.Stations).update({
            set("name", formData.name)
            set("paymentMethods", formData.paymentMethods.joinToString(","))
            set("imageUrl", newImageUrl ?: station.imageUrl)
            set("nearbyServices", formData.nearbyServices.joinToString(","))
            set("latitude", formData.location!!.latitude)
            set("longitude", formData.location!!.longitude)
        }) {
            filter {
                eq("id", station.id)
            }
            select()
        }.decodeSingle<SupaStation>()
        stationDao.update(supaStation.toEntity())

        val chargersRequests = formData.bundles.flatMap { it.chargers }.map {
            SupaCharger(
                stationId = supaStation.id!!,
                type = it.type.name,
                power = it.power.name,
                price = it.price,
                status = it.status.name,
                issue = it.issue.name
            )
        }
        val chargers = Supabase.table(SupaTable.Chargers).insert(chargersRequests) {
            select()
        }.decodeList<SupaCharger>()
        chargers.forEach { chargerDao.insert(it.toEntity()) }
    }

    suspend fun deleteStation(station: Station) {
        Supabase.table(SupaTable.Stations).delete {
            filter {
                eq("id", station.id)
            }
        }
        station.chargers.forEach { charger ->
            chargerDao.delete(charger.toEntity(station.id))
        }
        station.reviews.forEach { review ->
            reviewDao.delete(review.toEntity(station.id))
        }
        stationDao.delete(station.toEntity())
    }

    fun getStationsInRange(
        currentLocation: GeoLocation,
        radius: Double = 50.0
    ): Flow<List<Station>> {
        return stations.map { stationList ->
            stationList.filter { station ->
                val distance = calculateDistance(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    station.location.latitude,
                    station.location.longitude
                )
                distance <= radius
            }
        }
    }

    fun getStationsInRange(
        currentLocation: Location,
        radiusInMeters: Double = 8000.0
    ): Flow<List<Station>> {
        val geoLocation = GeoLocation(currentLocation.latitude, currentLocation.longitude)
        return getStationsInRange(geoLocation, radiusInMeters)
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}