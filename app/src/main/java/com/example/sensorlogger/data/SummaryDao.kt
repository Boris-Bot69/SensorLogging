package com.example.sensorlogger.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sensorlogger.model.*

@Dao
interface SummaryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addGravity(gravity: Summary)

    @Query("DELETE FROM gravity_data")
    suspend fun deleteAllGravity()

    @Query("SELECT * FROM gravity_data")
    fun readAllGravity(): LiveData<List<Gravity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAcceleration(acceleration: Summary)

    @Query("DELETE FROM acceleration_data")
    suspend fun deleteAllAcceleration()

    @Query("SELECT * FROM acceleration_data")
    fun readAllAcceleration(): LiveData<List<Acceleration>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addGyroscope(gyroscope: Summary)

    @Query ("DELETE FROM gyroscope_data")
    suspend fun deleteAllGyroscope()

    @Query("SELECT * FROM gyroscope_data")
    fun readAllGyroscope(): LiveData<List<Gyroscope>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addLocation(location: Summary)

    @Query("DELETE FROM location_data")
    suspend fun deleteAllLocation()

    @Query("SELECT * FROM location_data")
    fun readAllLocation(): LiveData<List<Location>>

    @Query("SELECT * from summary")
    fun readSummary(): LiveData<List<Summary>>

    @Query("DELETE FROM summary")
    fun deleteSummary()
}