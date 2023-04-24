package com.example.sensorlogger.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.sensorlogger.data.SummaryDatabase
import com.example.sensorlogger.model.*
import com.example.sensorlogger.repository.SummaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SummaryViewModel(application: Application): AndroidViewModel(application) {

    private val readAllGravity: LiveData<List<Gravity>>
    private val readAllAcceleration: LiveData<List<Acceleration>>
    private val readAllGyroscope: LiveData<List<Gyroscope>>
    private val readAllLocation: LiveData<List<Location>>
    private val readSummary: LiveData<List<Summary>>

    private val repository: SummaryRepository

    init {
        val summaryDao = SummaryDatabase.getDatabase(application).summaryDao()
        repository = SummaryRepository(summaryDao)
        readAllGravity = repository.readAllGravity
        readAllAcceleration = repository.readAllAcceleration
        readAllGyroscope = repository.readAllGyroscope
        readAllLocation = repository.readAllLocation
        readSummary = repository.readAllSummary

    }

    fun addAcceleration(acceleration: Summary) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addAcceleration(acceleration)
        }
    }

    fun deleteAcceleration() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllAcceleration()
        }
    }
    fun addGravity(gravity: Summary) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addGravity(gravity)
        }
    }

    fun deleteGravity() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllGravity()
        }
    }
    fun addGyroscope(gyroscope: Summary) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addGyroscope(gyroscope)
        }
    }

    fun deleteGyroscope() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllGyroscope()
        }
    }
    fun addLocation(location: Summary) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addLocation(location)
        }
    }

    fun deleteLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllLocation()
        }
    }

    fun readAllSummary() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.readAllSummary()
        }
    }

    fun deleteSummary() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSummary()
        }
    }

}