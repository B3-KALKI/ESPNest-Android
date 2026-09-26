package com.bharatupadhyay.espnest.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bharatupadhyay.espnest.domain.Device

@Entity(
    tableName = "devices",
    indices = [Index(value = ["ip", "port"], unique = true)]
)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val ip: String,
    val port: Int,
    val favorite: Boolean,
    val lastUsedAt: Long
)

fun DeviceEntity.toDomain(): Device = Device(
    id = id,
    name = name,
    ip = ip,
    port = port,
    favorite = favorite,
    lastUsedAt = lastUsedAt
)

fun Device.toEntity(): DeviceEntity = DeviceEntity(
    id = id,
    name = name,
    ip = ip,
    port = port,
    favorite = favorite,
    lastUsedAt = lastUsedAt
)
