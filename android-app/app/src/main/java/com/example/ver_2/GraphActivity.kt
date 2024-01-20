package com.example.ver_2


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*


class GraphActivity : AppCompatActivity() {
    //    private lateinit var plot: XYPlot
    private lateinit var plot: ImageView
    private lateinit var save: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        plot = findViewById(R.id.imageView)
        save = findViewById(R.id.save)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this@GraphActivity))
        }


        var eqn = intent.getStringExtra("lhs")
        var flag = intent.getIntExtra("flag", 0)

//        eqn = convertEquation(eqn)
        val py = Python.getInstance()

        val pyo = py.getModule("myscript")
        val obj = pyo.callAttr("main", eqn, flag)
        val str = obj.toString()

        val imageData: ByteArray = android.util.Base64.decode(str, android.util.Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        plot.setImageBitmap(bmp)

        save.setOnClickListener {
            saveImageToDownloads(this, bmp, "image.jpg")
            Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveImageToDownloads(context: Context, bitmap: Bitmap, fileName: String) {
        // Get the downloads directory
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Create the image file
        val file = File(downloadsDirectory, fileName)

        try {
            // Create an output stream to write the bitmap data to the file
            val outputStream: OutputStream = FileOutputStream(file)

            // Compress the bitmap and save it to the file
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

            // Flush and close the output stream
            outputStream.flush()
            outputStream.close()

            // Add the image to the Media Store gallery
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DATA, file.absolutePath)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}

