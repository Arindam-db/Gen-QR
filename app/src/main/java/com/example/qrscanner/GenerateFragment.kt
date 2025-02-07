package com.example.qrscanner

    import android.Manifest
    import android.annotation.SuppressLint
    import android.app.AlertDialog
    import android.content.ContentValues
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.graphics.Bitmap
    import android.net.Uri
    import android.os.Build
    import android.os.Bundle
    import android.os.Environment
    import android.provider.MediaStore
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageView
    import android.widget.Toast
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.annotation.RequiresApi
    import androidx.core.content.ContextCompat
    import androidx.fragment.app.Fragment
    import com.google.zxing.BarcodeFormat
    import com.google.zxing.WriterException
    import com.journeyapps.barcodescanner.BarcodeEncoder

class GenerateFragment : Fragment() {
    private lateinit var editTextInput: EditText
    private lateinit var buttonGenerate: Button
    private lateinit var imageViewQRCode: ImageView
    private var currentQRBitmap: Bitmap? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            currentQRBitmap?.let { saveAndShareQRCode(it) }
        } else {
            Toast.makeText(context, "Permission required to share QR code", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_generate, container, false).apply {
            initializeViews(this)
            setupListeners()
        }
    }

    private fun initializeViews(view: View) {
        editTextInput = view.findViewById(R.id.edit_text_input)
        buttonGenerate = view.findViewById(R.id.button_generate)
        imageViewQRCode = view.findViewById(R.id.image_view_qr_code)
    }

    private fun setupListeners() {
        buttonGenerate.setOnClickListener { generateQRCode() }
        imageViewQRCode.setOnLongClickListener {
            currentQRBitmap?.let { bitmap ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    saveAndShareQRCodeApi33(bitmap)
                } else {
                    checkAndRequestPermission(bitmap)
                }
            }
            true
        }
    }

    private fun generateQRCode() {
        val inputText = editTextInput.text.toString().trim()
        if (inputText.isEmpty()) {
            Toast.makeText(context, "Please enter text or URL", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val barcodeEncoder = BarcodeEncoder()
            currentQRBitmap = barcodeEncoder.encodeBitmap(inputText, BarcodeFormat.QR_CODE, 400, 400)
            imageViewQRCode.apply {
                setImageBitmap(currentQRBitmap)
                visibility = View.VISIBLE
            }
        } catch (e: WriterException) {
            Toast.makeText(context, "Error generating QR Code", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun checkAndRequestPermission(bitmap: Bitmap) {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                saveAndShareQRCode(bitmap)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun saveAndShareQRCodeApi33(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "QRCode_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val resolver = requireContext().contentResolver
        var imageUri: Uri? = null

        try {
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                imageUri = uri
                resolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                shareQRCode(uri)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving QR code", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            imageUri?.let { resolver.delete(it, null, null) }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun saveAndShareQRCode(bitmap: Bitmap) {
        val filename = "QRCode_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val resolver = requireContext().contentResolver
        var imageUri: Uri? = null

        try {
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                imageUri = uri
                resolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                shareQRCode(uri)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error saving QR code", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            imageUri?.let { resolver.delete(it, null, null) }
        }
    }

    private fun shareQRCode(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("This permission is needed to save and share QR codes")
            .setPositiveButton("Grant") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}