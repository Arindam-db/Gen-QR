package com.example.qrscanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Camera permission granted, navigate to scan fragment
            navigateToScanFragment()
        } else {
            Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
            // Navigate to generate fragment instead
            navigateToFragment(R.id.nav_generate)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(600)
        installSplashScreen()
        setContentView(R.layout.activity_main)


        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_scan -> checkAndRequestCameraPermission()
                    else -> navigateToFragment(item.itemId)
                }
                true
            }
        }

        if (savedInstanceState == null) {
            checkAndRequestCameraPermission()
        }
    }

    private fun checkAndRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                navigateToScanFragment()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Show permission explanation dialog
                showCameraPermissionRationale()
            }
            else -> {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showCameraPermissionRationale() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Camera Permission Required")
            .setMessage("The camera permission is needed to scan QR codes. Please grant the permission to use this feature.")
            .setPositiveButton("Grant") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Navigate to generate fragment instead
                navigateToFragment(R.id.nav_generate)
            }
            .create()
            .show()
    }

    private fun navigateToScanFragment() {
        navigateToFragment(R.id.nav_scan)
    }

    private fun navigateToFragment(itemId: Int) {
        val fragment = when (itemId) {
            R.id.nav_scan -> ScanFragment()
            R.id.nav_generate -> GenerateFragment()
            R.id.nav_history -> HistoryFragment()
            R.id.nav_setting -> SettingFragment()
            else -> null
        }

        fragment?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, it)
                .commit()
        }
    }
}