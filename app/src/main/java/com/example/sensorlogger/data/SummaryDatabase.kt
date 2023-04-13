package com.example.sensorlogger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sensorlogger.model.*

@Database(entities = [Acceleration::class, Gravity::class, Gyroscope::class, Location::class, Summary::class], version = 3, exportSchema = false)
abstract class SummaryDatabase: RoomDatabase() {

    abstract fun summaryDao() : SummaryDao

    companion object {
        @Volatile
        private var INSTANCE: SummaryDatabase? = null

        fun getDatabase(context: Context): SummaryDatabase {
            val tmp = INSTANCE
            if (tmp != null) {
                return tmp
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext,
                    SummaryDatabase::class.java, "summary").fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}