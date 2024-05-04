package com.example.cepstun.ui.activity

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.adevinta.leku.LATITUDE
import com.adevinta.leku.LONGITUDE
import com.adevinta.leku.LocationPickerActivity
import com.example.cepstun.R
import com.example.cepstun.databinding.ActivityRegisterBarberBinding

class RegisterBarberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBarberBinding

    private lateinit var dialog: AlertDialog

    private val lekuActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            try {
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val latitude = data?.getDoubleExtra(LATITUDE, 0.0)
                    val longitude = data?.getDoubleExtra(LONGITUDE, 0.0)
                    binding.ETCoordinate.setText("$latitude,$longitude")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this,
                    getString(R.string.error_map, e.printStackTrace()), Toast.LENGTH_SHORT).show()
            } finally {
                dialog.dismiss()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBarberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.tittle_dialog_Loading))
        builder.setMessage(getString(R.string.desc_dialog_RegisterBarber))
        builder.setCancelable(false)
        dialog = builder.create()

        binding.ETCoordinate.setOnClickListener {
            dialog.show()
            val locationPickerIntent = LocationPickerActivity.Builder(this)
                .withDefaultLocaleSearchZone()
                .shouldReturnOkOnBackPressed()
//                .withStreetHidden()
//                .withCityHidden()
//                .withZipCodeHidden()
//                .withSatelliteViewHidden()
                .withGooglePlacesEnabled()
                .withGoogleTimeZoneEnabled()
                .withVoiceSearchHidden()
                .withUnnamedRoadHidden()
                .withSearchBarHidden()
                .build()

            lekuActivityResultLauncher.launch(locationPickerIntent)
        }
    }
}