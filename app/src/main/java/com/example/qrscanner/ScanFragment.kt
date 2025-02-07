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
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class ScanFragment : Fragment() {
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var textScanResult: TextView
    private lateinit var buttonOpenLink: Button
    private lateinit var buttonCopy: Button
    private lateinit var buttonFlash: Button
    private lateinit var galleryIcon: ImageView
    private var isFlashOn = false
    private lateinit var database: ScannedQRDatabase
    private lateinit var vibrator: Vibrator

    // Launcher to pick image from gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { processSelectedImage(it) }
    }

    private fun processSelectedImage(uri: Uri) {
        try {
            val bitmap = loadBitmapFromUri(uri)
            scanBarcodeFromBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(R.string.failed_to_load_image)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        val contentResolver = requireContext().contentResolver
        return try {
            if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true) // Ensures bitmap is mutable
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw IOException("Failed to load bitmap from URI")
        }
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
    }

    private fun vibrateOnClick() {
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
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
        database = ScannedQRDatabase.getDatabase(requireContext())

        barcodeView.decodeContinuous(callback)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = requireContext().getSystemService(VibratorManager::class.java) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            requireContext().getSystemService(Vibrator::class.java) as Vibrator
        }

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

        buttonOpenLink.setOnClickListener {
            vibrateOnClick()
            val url = textScanResult.text.toString()
            if (url.isNotEmpty()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } else {
                showToast(R.string.no_url_to_open)
            }
        }

        buttonCopy.setOnClickListener {
            vibrateOnClick()
            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(getString(R.string.scanned_qr_code), textScanResult.text)
            clipboard.setPrimaryClip(clip)
            showToast(R.string.copied_to_clipboard)
        }

        galleryIcon.setOnClickListener {
            vibrateOnClick()
            pickImageLauncher.launch("image/*")
        }

        return view
    }

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text != null) {
                barcodeView.pause()
                textScanResult.text = result.text
                saveScannedData(result.text)
                buttonOpenLink.visibility = View.VISIBLE
                buttonCopy.visibility = View.VISIBLE
            }
        }

        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint?>?) {}
    }

    private fun saveScannedData(scannedText: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val scannedQR = ScannedQR(scannedText = scannedText)
            database.scannedQRDao().insertScannedQR(scannedQR)
        }
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
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, true)

            val intArray = IntArray(resizedBitmap.width * resizedBitmap.height)
            resizedBitmap.getPixels(intArray, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

            val source: LuminanceSource = RGBLuminanceSource(resizedBitmap.width, resizedBitmap.height, intArray)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = MultiFormatReader().decode(binaryBitmap)

            textScanResult.text = result.text
            buttonOpenLink.visibility = View.VISIBLE
            buttonCopy.visibility = View.VISIBLE

            saveScannedData(result.text)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(R.string.no_qr_code_found)
        }
    }
}
