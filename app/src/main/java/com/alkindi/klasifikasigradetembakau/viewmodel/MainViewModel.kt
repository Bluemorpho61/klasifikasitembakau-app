package com.alkindi.klasifikasigradetembakau.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
//import org.opencv.android.OpenCVLoader
//import org.opencv.android.Utils
//import org.opencv.core.Core
//import org.opencv.core.Mat
//import org.opencv.core.Size
//import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainViewModel(
    application: Application
) : AndroidViewModel(application = application) {
    private val _classificationResult = MutableLiveData<Pair<String, Float>?>()
    val classificationResult: LiveData<Pair<String, Float>?> = _classificationResult

    private val _inferenceTime = MutableLiveData<Long>()
    val inferenceTime: LiveData<Long> = _inferenceTime

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> = _errorMsg
    private val modelName: String = "model_mobilenet_v2.tflite"
    private val labelsName: String = "labels.txt"
    private lateinit var interpreter: Interpreter
    private lateinit var labels: List<String>

    init {
        setupOpenCV()
    }

    private fun setupOpenCV() {
//        if (!OpenCVLoader.initLocal()) {
//            Log.e(TAG, "OpenCV gagal di inisialisasi")
//            _errorMsg.postValue("Gagal menginisialisasi OpenCV")
//        } else {
//            Log.d(TAG, "OpenCV berhasil di inisialisasi")
            setupImageClassifier()
//        }
    }

    private fun setupImageClassifier() {
        try {
            val modelBuffer = FileUtil.loadMappedFile(getApplication(), modelName)
            interpreter = Interpreter(modelBuffer, Interpreter.Options())

            labels = FileUtil.loadLabels(getApplication(), labelsName)
        } catch (e: Exception) {
            _errorMsg.postValue("Gagal menginisialisasi classifier: ${e.message}")
            Log.e(TAG, "Error Initializing Classifier ", e)
        }
    }

//    TODO: UBAH FUNCTION GAMBAR MENJADI BITMAP

    fun getImgToClassify(imgUri: Uri) {
        viewModelScope.launch {
            _isLoading.postValue(true)

            val startTime = SystemClock.uptimeMillis()
            try {
                val originalBitmap = toBitmap(imgUri)
                if (originalBitmap == null) {
                    _errorMsg.postValue("Gagal mengambil URI Gambar")
                    _isLoading.postValue(false)
                    return@launch
                }

//                val rgbMat = Mat()
//                Utils.bitmapToMat(originalBitmap, rgbMat)
//                Imgproc.cvtColor(rgbMat, rgbMat, Imgproc.COLOR_RGBA2RGB)
//
//                val labMat = Mat()
//                Imgproc.cvtColor(rgbMat, labMat, Imgproc.COLOR_RGB2Lab)
//
//                val labChannels = ArrayList<Mat>()
//                Core.split(labMat, labChannels)
//                val lChannel = labChannels[0]
//
//                val clahe = Imgproc.createCLAHE(2.0, Size(8.0, 8.0))
//                val lClahe = Mat()
//                clahe.apply(lChannel, lClahe)
//
//                labChannels[0] = lClahe
//                val labClaheMat = Mat()
//                Core.merge(labChannels, labClaheMat)
//
//                val rgbClaheMat = Mat()
//                Imgproc.cvtColor(labClaheMat, rgbClaheMat, Imgproc.COLOR_Lab2RGB)
//
//                val claheBitmap = Bitmap.createBitmap(
//                    originalBitmap.width,
//                    originalBitmap.height,
//                    Bitmap.Config.ARGB_8888
//                )
                val imageProcessor = ImageProcessor.Builder()
                    .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                    .add(NormalizeOp(0f, 255f))
                    .build()

                var tensorImage = TensorImage.fromBitmap(originalBitmap)
                tensorImage = imageProcessor.process(tensorImage)

                val outputShape = interpreter.getOutputTensor(0).shape()
                val outputDataType = interpreter.getOutputTensor(0).dataType()
                val outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType)

                interpreter.run(tensorImage.buffer, outputBuffer.buffer.rewind())

                val scores = outputBuffer.floatArray
                var maxScore = -1f
                var maxIndex = -1
                scores.forEachIndexed { index, score ->
                    if (score > maxScore) {
                        maxScore = score
                        maxIndex = index
                    }
                }

                if (maxIndex != -1) {
                    val resultLabel = labels[maxIndex]
                    _classificationResult.postValue(Pair(resultLabel, maxScore))
                }
            } catch (e: Exception) {
                _errorMsg.postValue("Error Saat melakukan klasifikasi: ${e.message}")
                Log.e(TAG, "Error during classification", e)
            } finally {
                val inferenceTime = SystemClock.uptimeMillis() - startTime
                _inferenceTime.postValue(inferenceTime)
                _isLoading.postValue(false)
            }

        }
    }

    private fun toBitmap(uri: Uri): Bitmap? =
        try {
            val context = getApplication<Application>().contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context, uri)
            }.copy(Bitmap.Config.ARGB_8888, true)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengkonversi Uri ke Bitmap: ${e.message}")
            _errorMsg.postValue("Gagal melakukan konversi gambar")
            null
        }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }
}