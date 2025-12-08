package com.alkindi.klasifikasigradetembakau.activity

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.alkindi.klasifikasigradetembakau.databinding.ActivityMainBinding
import com.alkindi.klasifikasigradetembakau.getImageUri
import com.alkindi.klasifikasigradetembakau.saveCorrectlyOrientedImage
import com.alkindi.klasifikasigradetembakau.viewmodel.MainViewModel


class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var currentImageUri: Uri? = null

    private lateinit var sensorManager: SensorManager
    private var sensorLight: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        enableEdgeToEdge()
        setContentView(view)


        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (sensorLight == null) showToast("Sensor cahaya tidak ditemukan pada perangkat ini")
        binding.luxMeterValue.text = "Sensor tidak ditemukan"
        observeViewModelData()

        binding.btnConfirm.setOnClickListener {
            currentImageUri?.let {
                analyzeImage(it)
            } ?: run {
                showToast("Silahkan Pilih Gambar Terlebih Dahulu")
            }
        }

        binding.btnCamera.setOnClickListener {
            startCamera()
        }

        binding.btnGallery.setOnClickListener {
            startGallery()
        }

    }

    private fun observeViewModelData() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarClassify.isVisible = isLoading
            binding.btnConfirm.isVisible = !isLoading
        }

        viewModel.errorMsg.observe(this) { errorMsg ->
            if (errorMsg.isNotEmpty())
                showToast(errorMsg)
        }

        viewModel.classificationResult.observe(this) { res ->
            if (res != null) {
                val label = res.first
                val score = res.second

                val inferenceTime = viewModel.inferenceTime.value ?: 0L

                toResult(
                    uri = currentImageUri!!,
                    label = label,
                    score = score,
                    inferenceTime = inferenceTime
                )
            }
        }

        viewModel.luxValue.observe(this) { lux ->
            binding.luxMeterValue.text ="${lux.toInt()} Lux"
        }
    }

    private fun toResult(uri: Uri, label: String, inferenceTime: Long, score: Float) {
        val toClassifierResult = Intent(this, ClassifierResultActivity::class.java).apply {
            putExtra(ClassifierResultActivity.EXTRA_IMAGE_URI, uri.toString())
            putExtra(ClassifierResultActivity.EXTRA_LABEL, label)
            putExtra(ClassifierResultActivity.EXTRA_SCORE, score)
            putExtra(ClassifierResultActivity.EXTRA_INFERENCE_TIME, inferenceTime)
        }
        startActivity(toClassifierResult)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun analyzeImage(image: Uri) {
        viewModel.getImgToClassify(image)
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d(TAG, "No Image Selected")
        }
    }

    private fun startCamera() {
        val imageUri = getImageUri(this)
        currentImageUri = imageUri
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess && currentImageUri != null) {
            saveCorrectlyOrientedImage(this, currentImageUri!!)
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d(TAG, "Show Image: $it")
            binding.imCitra.setImageURI(it)
        }
        hideNshowComponentAfterSelectingImage()
    }

    private fun hideNshowComponentAfterSelectingImage() {
        binding.btnConfirm.visibility = View.VISIBLE
        binding.tvInstruction.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        sensorLight?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            viewModel.updateLux(lux)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        
    }


    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}