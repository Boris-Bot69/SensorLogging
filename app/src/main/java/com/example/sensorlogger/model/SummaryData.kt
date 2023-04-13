package com.example.sensorlogger.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "summary")
data class Summary(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo val time: String,
    @Embedded val gravity: Gravity?,
    @Embedded val acceleration: Acceleration?,
    @Embedded val gyroscope: Gyroscope?,
    @Embedded val location: Location?
    )

@Entity(tableName = "gravity_data")
data class Gravity(
    @PrimaryKey(autoGenerate = true) val gravityid: Int,
    @ColumnInfo val gravX: String,
    @ColumnInfo val gravY: String,
    @ColumnInfo val gravZ: String,
)

@Entity(tableName = "Acceleration_data")
data class Acceleration(
    @PrimaryKey(autoGenerate = true) val accelerationid: Int,
    @ColumnInfo val AccelX: String,
    @ColumnInfo val AccelY: String,
    @ColumnInfo val AccelZ: String,
)
@Entity(tableName = "gyroscope_data")
data class Gyroscope (
    @PrimaryKey(autoGenerate = true) val gyroscopeid: Int,
    @ColumnInfo val angular_velocity_x: String,
    @ColumnInfo val angular_velocity_y: String,
    @ColumnInfo val angular_velocity_z: String,
    )


@Entity(tableName = "location_data")
data class Location(
    @PrimaryKey(autoGenerate = true) val locationid: Int,
    @ColumnInfo val latitude: String,
    @ColumnInfo val longitude: String,
)


