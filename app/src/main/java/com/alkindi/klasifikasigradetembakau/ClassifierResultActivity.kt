package com.alkindi.klasifikasigradetembakau

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alkindi.klasifikasigradetembakau.databinding.ActivityClassifierResultBinding

class ClassifierResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClassifierResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityClassifierResultBinding.inflate(layoutInflater)
        val view = binding.root
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        val imgUri = intent.getStringExtra(EXTRA_IMAGE_URI)
        val score = intent.getFloatExtra(EXTRA_SCORE, 0f)
        val inferenceTime = intent.getLongExtra(EXTRA_INFERENCE_TIME, 0L)
        val label = intent.getStringExtra(EXTRA_LABEL)

        if (imgUri != null && label != null) {
            val imageUri = imgUri.toUri()
            binding.imCitra.setImageURI(imageUri)
            binding.gradeLetter.text = label
            binding.tvScore.text = score.toString()
//            binding.tvInferenceTime.text =inferenceTime.toString()
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_LABEL = "extra_image_label"
        const val EXTRA_INFERENCE_TIME = "extra_inference_time"
        const val EXTRA_SCORE = "extra_score"
        private val TAG = ClassifierResultActivity::class.java.simpleName
    }
}