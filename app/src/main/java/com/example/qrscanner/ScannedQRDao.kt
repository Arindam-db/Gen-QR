package com.example.qrscanner

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedQRDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScannedQR(scannedQR: ScannedQR)

    @Query("SELECT * FROM scanned_qr_table ORDER BY timestamp DESC")
    fun getAllScannedQR(): Flow<List<ScannedQR>>

    @Query("DELETE FROM scanned_qr_table")
    suspend fun clearHistory()
}