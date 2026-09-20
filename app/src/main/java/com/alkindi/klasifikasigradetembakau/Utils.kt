package com.alkindi.klasifikasigradetembakau

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
private val timeStamp: String = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())

fun getImageUri(context: Context): Uri {
    var uri: Uri? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$timeStamp.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MyCamera/")
        }
        uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }
    return uri ?: getImageUriForPreq(context)
}

fun getImageUriForPreq(context: Context): Uri {
    val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File(filesDir, "/MyCamera/$timeStamp.jpg")
    if (imageFile.parentFile?.exists() == false) imageFile.parentFile?.mkdir()
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        imageFile
    )
}

fun saveCorrectlyOrientedImage(context: Context, imageUri: Uri) {
    val inputStream = context.contentResolver.openInputStream(imageUri) ?: return
    val exif = ExifInterface(inputStream)
    inputStream.close()

    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)

    val orientation =
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val rotationDegrees = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }

    val rotatedBitmap = if (rotationDegrees != 0f) {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees)
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }

    // Save the rotated bitmap back to the same URI
    val outputStream: OutputStream? = context.contentResolver.openOutputStream(imageUri)
    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream!!)
    outputStream.flush()
    outputStream.close()
}

fun showToastShort(msg: String, context: Context) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun saveImageToGallery(context: Context, imageUri: Uri, label: String) {
    val timeStampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val fileName = "${timeStampStr}_${label}.jpg"

    // Format for overlay: "Waktu Klasifikasi: 07.00 / 16 September 2026"
    val localeId = Locale("id", "ID")
    val timeOverlay = SimpleDateFormat("HH.mm", localeId).format(Date())
    val dateOverlay = SimpleDateFormat("dd MMMM yyyy", localeId).format(Date())
    val fullOverlay = "Waktu Klasifikasi: $timeOverlay / $dateOverlay"

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/KlasifikasiTembakau")
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    if (uri != null) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(resolver, imageUri)
            val annotatedBitmap = addTextToBitmap(bitmap, label, fullOverlay)

            val outputStream = resolver.openOutputStream(uri)
            if (outputStream != null) {
                annotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                showToastShort("Berhasil menyimpan gambar ke galeri", context)
            }
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            showToastShort("Gagal menyimpan gambar: ${e.message}", context)
        }
    } else {
        showToastShort("Gagal menyimpan gambar", context)
    }
}

private fun addTextToBitmap(bitmap: Bitmap, label: String, timestamp: String): Bitmap {
    val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(resultBitmap)

    val width = resultBitmap.width
    val height = resultBitmap.height

    // Scale sizes relative to image width
    val labelSize = width * 0.1f
    val timeSize = width * 0.04f
    val margin = width * 0.05f

    val paint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
    }

    // Draw Label (Grade)
    paint.apply {
        textSize = labelSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val labelY = height - margin - timeSize - (width * 0.02f)
    canvas.drawText(label, margin, labelY, paint)

    // Draw Timestamp
    paint.apply {
        textSize = timeSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    canvas.drawText(timestamp, margin, height - margin, paint)

    return resultBitmap
}