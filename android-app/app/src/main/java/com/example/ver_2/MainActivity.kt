package com.example.ver_2

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 200
    private val CAMERA_REQUEST_CODE = 300
    private val CROP_REQUEST_CODE = 400
    private val SERVER_URL =
        "https://5432-14-139-61-131.ngrok-free.app/name"

    private val cropActivityResultContract = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity().setAspectRatio(16, 9)
                .getIntent(this@MainActivity)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }

    }

    private var imageUri: Uri? = null
    private var imageBitmap: Bitmap? = null
    private lateinit var statusTextView: TextView
    private lateinit var progress: ProgressBar
    private lateinit var imageView: ImageView
    private lateinit var plot: Button
    private lateinit var plot3d: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        val galleryButton = findViewById<Button>(R.id.galleryButton)
        val cameraButton = findViewById<Button>(R.id.cameraButton)
        plot = findViewById<Button>(R.id.graphButton)
        plot3d = findViewById(R.id.graph3dButton)
        statusTextView = findViewById(R.id.status)
        progress = findViewById(R.id.progressBar_cyclic)




        galleryButton.setOnClickListener {
            imageView.setImageBitmap(null)
            chooseImageFromGallery()
        }

        cameraButton.setOnClickListener {
            imageView.setImageBitmap(null)
            checkCameraPermissionAndCaptureImage()
        }

    }

    private fun processImage() {
        statusTextView.text = "Please wait..."
        progress.visibility = View.VISIBLE


        GlobalScope.launch(Dispatchers.IO) {
            // Convert Bitmap to byte array
            val outputStream = ByteArrayOutputStream()
            imageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            val imageBytes = outputStream.toByteArray()
            outputStream.write(imageBytes)

            // Perform network request and receive response
            Log.d("A", "processImage: $imageBytes")
            val response = performNetworkRequest(imageBytes)

            withContext(Dispatchers.Main) {
                // Update UI with the response
//                Toast.makeText(this@MainActivity, "$response", Toast.LENGTH_SHORT).show()
                progress.visibility = View.GONE
                var t=response.toString().split(" ")
                var disp=""
                for(i in 1 until t.size){
                    disp+=t[i]
                    disp+=" "
               }
                statusTextView.text = "The equation is: $disp"
                var equation = response.toString()

// Separate the left-hand side and right-hand side
                equation = equation.split(' ')[0].trim()
                Log.d("A", "lhs:$equation ")
                openGraph(equation)

            }
        }
    }


    private fun openGraph(lhs: String) {
        if (lhs == "") {
            Toast.makeText(this@MainActivity, "Equation Not Found", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("A", "openGraph: $lhs")
//            Toast.makeText(this@MainActivity, "$lhs", Toast.LENGTH_SHORT).show()
//            plot.visibility = View.VISIBLE
            plot.setOnClickListener {
                // The integer value to send
                val intent = Intent(this, GraphActivity::class.java)
                intent.putExtra("lhs", lhs)
                intent.putExtra("flag",0)
                startActivity(intent)
            }
            plot3d.setOnClickListener {
                val intent =Intent(this, GraphActivity::class.java)
                intent.putExtra("lhs", lhs)
                intent.putExtra("flag", 1)
                startActivity(intent)
            }
        }
    }

    private fun chooseImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun checkCameraPermissionAndCaptureImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            captureImageFromCamera()
        }
    }

    private fun captureImageFromCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "Camera Image")
            put(MediaStore.Images.Media.DESCRIPTION, "Image captured from camera")
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImageFromCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    imageView.setImageBitmap(null)
                    val uri: Uri = data?.data!!
                    val imageStream = contentResolver.openInputStream(uri)
                    imageBitmap = BitmapFactory.decodeStream(imageStream)
                    imageView.setImageBitmap(imageBitmap)
                    imageView.visibility = View.VISIBLE
                    processImage()
                }

                CAMERA_REQUEST_CODE -> {
                    imageView.setImageBitmap(null)
                    imageUri?.let {
                        CropImage.activity(imageUri)
                            .setAspectRatio(12, 4)
                            .setFixAspectRatio(false)
                            .start(this@MainActivity)
                    }
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result: CropImage.ActivityResult? = CropImage.getActivityResult(data)
                    if (resultCode == RESULT_OK) {
                        val croppedUri: Uri? = result?.uri
                        val imageStream = contentResolver.openInputStream(croppedUri!!)
                        imageBitmap = BitmapFactory.decodeStream(imageStream)
                        imageView.setImageBitmap(imageBitmap)
                        imageView.visibility = View.VISIBLE
                        processImage()
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        val error: Exception? = result?.error
                        // Handle the crop error if necessary
                    }
                }
            }
        }
    }


    private fun performNetworkRequest(imageBytes: ByteArray): String {
        val client = OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS) // Set the connect timeout to 10 seconds
            .writeTimeout(300, TimeUnit.SECONDS) // Set the write timeout to 10 seconds
            .readTimeout(300, TimeUnit.SECONDS) // Set the read timeout to 30 seconds
            .build()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "image.png",
                RequestBody.create("image/*".toMediaTypeOrNull(), imageBytes)
            )
            .build()

        val request = Request.Builder()
            .url(SERVER_URL)
            .post(requestBody)
            .build()
        try {
            val response: Response = client.newCall(request).execute()
            Log.d("A", "successful")
            if (response.isSuccessful)
                return response.body?.string() ?: ""
        } catch (e: IOException) {
            e.printStackTrace()
        }


        return ""
    }


}

