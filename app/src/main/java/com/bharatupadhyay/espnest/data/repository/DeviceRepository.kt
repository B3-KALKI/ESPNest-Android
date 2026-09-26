package com.bharatupadhyay.espnest.data.repository

import com.bharatupadhyay.espnest.data.local.DeviceDao
import com.bharatupadhyay.espnest.data.local.DeviceEntity
import com.bharatupadhyay.espnest.data.local.toDomain
import com.bharatupadhyay.espnest.data.local.toEntity
import com.bharatupadhyay.espnest.domain.ConnectionTarget
import com.bharatupadhyay.espnest.domain.Device
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeviceRepository(private val dao: DeviceDao) {

    fun observeDevices(): Flow<List<Device>> = dao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getById(id: Long): Device? = dao.getById(id)?.toDomain()

    suspend fun getByAddress(ip: String, port: Int): Device? =
        dao.getByAddress(ip.trim(), port)?.toDomain()

    suspend fun isSaved(ip: String, port: Int): Boolean =
        dao.getByAddress(ip.trim(), port) != null

    suspend fun save(name: String, ip: String, port: Int, favorite: Boolean = false): Device {
        val existing = dao.getByAddress(ip.trim(), port)
        val now = System.currentTimeMillis()
        return if (existing != null) {
            val updated = existing.copy(
                name = name.trim().ifBlank { existing.name },
                lastUsedAt = now
            )
            dao.update(updated)
            updated.toDomain()
        } else {
            val id = dao.insert(
                DeviceEntity(
                    name = name.trim().ifBlank { "ESP32" },
                    ip = ip.trim(),
                    port = port,
                    favorite = favorite,
                    lastUsedAt = now
                )
            )
            Device(id = id, name = name.trim().ifBlank { "ESP32" }, ip = ip.trim(), port = port, favorite = favorite, lastUsedAt = now)
        }
    }

    suspend fun update(device: Device) {
        dao.update(device.toEntity())
    }

    suspend fun delete(device: Device) {
        dao.delete(device.toEntity())
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun toggleFavorite(device: Device) {
        dao.setFavorite(device.id, !device.favorite)
    }

    suspend fun markLastUsed(ip: String, port: Int) {
        dao.updateLastUsedByAddress(ip.trim(), port, System.currentTimeMillis())
    }

    suspend fun seedIfEmpty() {
        if (dao.count() > 0) return
        val now = System.currentTimeMillis()
        dao.insert(
            DeviceEntity(
                name = "VeerKavach",
                ip = "192.168.4.1",
                port = 80,
                favorite = true,
                lastUsedAt = now
            )
        )
        dao.insert(
            DeviceEntity(
                name = "Garden Controller",
                ip = "192.168.1.105",
                port = 80,
                favorite = true,
                lastUsedAt = now - 60_000
            )
        )
        dao.insert(
            DeviceEntity(
                name = "ESP32 Sensor",
                ip = "192.168.1.120",
                port = 80,
                favorite = false,
                lastUsedAt = now - 120_000
            )
        )
    }

    companion object {
        fun defaultName(target: ConnectionTarget): String {
            val provided = target.name?.trim().orEmpty()
            return provided.ifBlank { "ESP32 ${target.ip}" }
        }
    }
}
