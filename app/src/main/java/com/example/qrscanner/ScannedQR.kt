package com.example.qrscanner

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_qr_table")
data class ScannedQR(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scannedText: String,
    val timestamp: Long = System.currentTimeMillis()
)