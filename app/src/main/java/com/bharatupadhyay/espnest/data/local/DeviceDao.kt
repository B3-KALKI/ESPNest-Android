package com.bharatupadhyay.espnest.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY favorite DESC, lastUsedAt DESC")
    fun observeAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices ORDER BY favorite DESC, lastUsedAt DESC")
    suspend fun getAll(): List<DeviceEntity>

    @Query("SELECT COUNT(*) FROM devices")
    suspend fun count(): Int

    @Query("SELECT * FROM devices WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DeviceEntity?

    @Query("SELECT * FROM devices WHERE ip = :ip AND port = :port LIMIT 1")
    suspend fun getByAddress(ip: String, port: Int): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(device: DeviceEntity): Long

    @Update
    suspend fun update(device: DeviceEntity)

    @Delete
    suspend fun delete(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM devices")
    suspend fun deleteAll()

    @Query("UPDATE devices SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Long, timestamp: Long)

    @Query("UPDATE devices SET lastUsedAt = :timestamp WHERE ip = :ip AND port = :port")
    suspend fun updateLastUsedByAddress(ip: String, port: Int, timestamp: Long)

    @Query("UPDATE devices SET favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)
}
