package com.example.cepstun

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cepstun.data.local.ModelDataList
import com.example.cepstun.databinding.ActivityAirecomendationBinding
import com.example.cepstun.ui.activity.CameraActivity
import com.example.cepstun.ui.activity.CameraActivity.Companion.CAMERAX_RESULT
import com.example.cepstun.ui.adapter.ModelAdapter

class AIRecomendationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAirecomendationBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ModelAdapter

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAirecomendationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.RVModelRecomend

        // sementara ngambil data dari object dulu sambil nunggu model ML dan data CC
        adapter = ModelAdapter(ModelDataList.modelDataValue)

        showRecyclerList()

//        if (currentImageUri == null){
//            openCamera()
//        } else {
//            showImage()
//        }
        currentImageUri = intent.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
        showImage()
    }

//    fun openCamera() {
//        val intent = Intent(this, CameraActivity::class.java)
//        launcherIntentCameraX.launch(intent)
//    }
//    private val launcherIntentCameraX = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) {
//        if (it.resultCode == CAMERAX_RESULT) {
//            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
//            showImage()
//        }
//    }

    private fun showImage() {
        currentImageUri?.let {
            binding.IVPhoto.setImageURI(it)
        }
//        dialog.dismiss()
    }

    private fun showRecyclerList() {
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
    }
}