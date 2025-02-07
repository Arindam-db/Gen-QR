package com.example.qrscanner

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.ResultPoint
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import java.io.IOException

class ScanFragment : Fragment() {
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var textScanResult: TextView
    private lateinit var buttonOpenLink: Button
    private lateinit var buttonCopy: Button
    private lateinit var buttonFlash: Button
    private lateinit var galleryIcon: ImageView
    private var isFlashOn = false
    private lateinit var vibrator: Vibrator

    // Launcher to pick image from gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { processSelectedImage(it) }
    }

    private fun processSelectedImage(uri: Uri) {
        try {
            val bitmap = loadBitmapFromUri(uri)
            scanBarcodeFromBitmap(bitmap)
        } catch (_: IOException) {
            showToast(R.string.failed_to_load_image)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        val contentResolver = requireContext().contentResolver
        return if (Build.VERSION.SDK_INT < 28) {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
    }

    private fun vibrateOnClick() {
        // Check if the device has a vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(100) // Increase duration to 100ms
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scan, container, false)

        barcodeView = view.findViewById(R.id.barcode_scanner)
        textScanResult = view.findViewById(R.id.text_scan_result)
        buttonOpenLink = view.findViewById(R.id.button_open_link)
        buttonCopy = view.findViewById(R.id.button_copy)
        buttonFlash = view.findViewById(R.id.button_flash)
        galleryIcon = view.findViewById(R.id.gallery_icon)

        // Start continuous scanning
        barcodeView.decodeContinuous(callback)

        // Get the Vibrator service
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = requireContext().getSystemService(VibratorManager::class.java) as VibratorManager
        vibratorManager.defaultVibrator
            } else {
            @Suppress("DEPRECATION")
            requireContext().getSystemService(Vibrator::class.java) as Vibrator
                    }

        // Flashlight toggle
        buttonFlash.setOnClickListener {
            isFlashOn = !isFlashOn
            if (isFlashOn) {
                barcodeView.setTorchOn()
                buttonFlash.text = getString(R.string.flash_off)
            } else {
                barcodeView.setTorchOff()
                buttonFlash.text = getString(R.string.flash_on)
            }
            vibrateOnClick()
        }


        // Open link action
        buttonOpenLink.setOnClickListener {
            vibrateOnClick()
            val url = textScanResult.text.toString()
            if (url.isNotEmpty()) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            } else {
                showToast(R.string.no_url_to_open)
            }
        }

        // Copy action
        buttonCopy.setOnClickListener {
            vibrateOnClick()
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(getString(R.string.scanned_qr_code), textScanResult.text)
            clipboard.setPrimaryClip(clip)
            showToast(R.string.copied_to_clipboard)
        }

        // Gallery icon click listener to pick an image
        galleryIcon.setOnClickListener {
            vibrateOnClick()
            pickImageLauncher.launch("image/*")
        }

        return view
    }


    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text != null) {
                barcodeView.pause() // Pause the scanner after reading the code
                textScanResult.text = result.text

                // Store scanned data in SharedPreferences
                saveScannedData(result.text)

                // Make the buttons visible
                buttonOpenLink.visibility = View.VISIBLE
                buttonCopy.visibility = View.VISIBLE
            }
        }

        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint?>?) {}
    }

    // Function to store scanned QR code data in SharedPreferences
    private fun saveScannedData(scannedText: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("QR_HISTORY", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Retrieve existing data and append new entry
        val existingData = sharedPreferences.getStringSet("history_list", mutableSetOf()) ?: mutableSetOf()
        existingData.add(scannedText)

        // Save updated history
        editor.putStringSet("history_list", existingData)
        editor.apply()
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    private fun scanBarcodeFromBitmap(bitmap: Bitmap) {
        try {
            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(
                intArray,
                0,
                bitmap.width,
                0,
                0,
                bitmap.width,
                bitmap.height
            )
            val source: LuminanceSource =
                RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = MultiFormatReader().decode(binaryBitmap)

            // Display result
            textScanResult.text = result.text
            buttonOpenLink.visibility = View.VISIBLE
            buttonCopy.visibility = View.VISIBLE
        } catch (_: Exception) {
            showToast(R.string.no_qr_code_found)
        }
    }
}