package com.example.qrscanner

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScannedQR::class], version = 1, exportSchema = false)
abstract class ScannedQRDatabase : RoomDatabase() {

    abstract fun scannedQRDao(): ScannedQRDao

    companion object {
        @Volatile
        private var INSTANCE: ScannedQRDatabase? = null

        fun getDatabase(context: Context): ScannedQRDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScannedQRDatabase::class.java,
                    "scanned_qr_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}