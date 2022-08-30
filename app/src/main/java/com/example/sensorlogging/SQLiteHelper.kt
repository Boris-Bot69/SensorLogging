package com.example.sensorlogging

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.sql.Timestamp

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "recording.db"
        private const val ID = "id"
        private const val TIMESTAMP = "timestamp"
        private const val FILENAME = "Logging_Name"
        private const val FILETYPE = "type"
    }



    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE $FILENAME ($ID INTEGER PRIMARY KEY AUTOINCREMENT, $TIMESTAMP TEXT, $FILETYPE TEXT);")
        db?.execSQL(createTable)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $FILENAME")
        onCreate(db)

    }

    fun addFile(timestamp: String, type : String) {
        val values = ContentValues()
        values.put(TIMESTAMP, timestamp)
        values.put(FILETYPE,type)
        val db = this.writableDatabase
        var result = db.insert(DATABASE_NAME,null,values)

    }

    fun getName(): Cursor? {

        // here we are creating a readable
        // variable of our database
        // as we want to read value from it
        val db = this. readableDatabase

        // below code returns a cursor to
        // read data from the database
        return db.rawQuery("SELECT * FROM $DATABASE_NAME", null)

    }


}