package com.alkindi.klasifikasigradetembakau

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.alkindi.klasifikasigradetembakau.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        enableEdgeToEdge()
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.btnConfirm.setOnClickListener {
            val toClassifierResult = Intent(this, ClassifierResultActivity::class.java)
            startActivity(toClassifierResult)
        }

        binding.btnCamera.setOnClickListener {
            startCamera()
        }


    }

    private fun startCamera() {
        val imageUri = getImageUri(this)
        currentImageUri =imageUri
        launcherIntentCamera.launch(currentImageUri!!)
        //        currentImageUri = getImageUri(this)
//        launcherIntentCamera.launch(currentImageUri)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess &&currentImageUri !=null) {
            saveCorrectlyOrientedImage(this, currentImageUri!!)
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d(TAG, "Show Image: $it")
            binding.imCitra.setImageURI(it)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}