package com.example.sensorlogger.repository

import androidx.lifecycle.LiveData
import com.example.sensorlogger.data.SummaryDao
import com.example.sensorlogger.model.*

class SummaryRepository (private val summaryDao: SummaryDao){

    val readAllGravity: LiveData<List<Gravity>> = summaryDao.readAllGravity()
    val readAllAcceleration: LiveData<List<Acceleration>> = summaryDao.readAllAcceleration()
    val readAllGyroscope: LiveData<List<Gyroscope>> = summaryDao.readAllGyroscope()
    val readAllLocation: LiveData<List<Location>> = summaryDao.readAllLocation()
    val readAllSummary: LiveData<List<Summary>> = summaryDao.readSummary()


    suspend fun addGravity(gravity: Summary) {
        summaryDao.addGravity(gravity)
    }

    suspend fun deleteAllGravity() {
        summaryDao.deleteAllGravity()
    }

    suspend fun addAcceleration(acceleration: Summary) {
        summaryDao.addAcceleration(acceleration)
    }

    suspend fun deleteAllAcceleration() {
        summaryDao.deleteAllAcceleration()
    }

    suspend fun addGyroscope(gyroscope: Summary) {
        summaryDao.addGyroscope(gyroscope)
    }

    suspend fun deleteAllGyroscope() {
        summaryDao.deleteAllGyroscope()
    }

    suspend fun addLocation(location: Summary) {
        summaryDao.addLocation(location)
    }
    suspend fun deleteAllLocation() {
        summaryDao.deleteAllLocation()
    }

    suspend fun readAllSummary() {
        summaryDao.readSummary()
    }





}