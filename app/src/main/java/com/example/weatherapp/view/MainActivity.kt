package com.example.weatherapp.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.view.inputmethod.InputMethodManager


class MainActivity : AppCompatActivity() {

    val MY_PERMISSIONS_REQUEST_LOCATION = 99

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding : ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()

        viewModel.dataResponse
            .observe(this, Observer { data ->
                if (data != null) {
                    binding.layoutLocation.visibility = View.VISIBLE
                    binding.layoutSearch.visibility = View.GONE
                    binding.tvLocation.text = data.location
                    binding.tvWeather.text = data.weatherList[0].main
                    binding.tvDescription.text = data.weatherList[0].description
                    binding.tvTemperature.text =
                        viewModel.convertKelvinToCelsius(data.info.temperature).toString() + getString(
                            R.string.unit_temp
                        )
                    binding.tvPressure.text = data.info.pressure + getString(R.string.unit_pressure)
                    binding.tvHumidity.text = data.info.humidity + getString(R.string.unit_humidity)
                    viewModel.imageUrl.value = "http://openweathermap.org/img/wn/${data.weatherList[0].icon}@2x.png"
                } else {
                    handleError("Load data failed")
                }
            })

        viewModel.errorMsg
            .observe(this, Observer { msg ->
                currentFocus?.let {
                    val inputManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken,HIDE_NOT_ALWAYS)
                }
                handleError(msg)
            })

        viewModel.progressState
            .observe(this, Observer { isProgress ->
                if (isProgress) {
                    startProgressBar()
                } else {
                    stopProgressBar()
                }
            })

        binding.btnSearch.setOnClickListener {
            binding.layoutLocation.visibility = View.GONE
            binding.layoutSearch.visibility = View.VISIBLE
        }

        binding.btnGoSearch.setOnClickListener {
            currentFocus?.let {
                val inputManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
            }
            viewModel.loadDataByCity()
        }

        binding.etCity.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEARCH-> {
                    currentFocus?.let {
                        val inputManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputManager.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
                    }
                    viewModel.loadDataByCity()
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.add("Refresh")
        menu.getItem(0)
            .setIcon(R.drawable.ic_menu_refresh)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_WITH_TEXT)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            0 -> {
                getLastLocation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val locationCallback = object : LocationCallback() {
        @SuppressLint("SetTextI18n")
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            viewModel.setLat(lastLocation.latitude)
            viewModel.setLong(lastLocation.longitude)
            viewModel.loadDataByCoord()
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            } else {
                Toast.makeText(this, "permission not granted", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("SetTextI18n")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        viewModel.setLat(location.latitude)
                        viewModel.setLong(location.longitude)
                        viewModel.loadDataByCoord()
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    private fun requestNewLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.myLooper()
        )
    }

    private fun handleError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    fun startProgressBar() {
        binding.layoutLocation.visibility = View.GONE
        binding.layoutWeather.visibility = View.GONE
        binding.cardTemp.visibility = View.GONE
        binding.cardPressure.visibility = View.GONE
        binding.cardHumidity.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    fun stopProgressBar() {
        binding.progressBar.visibility = View.GONE
        binding.layoutLocation.visibility = View.VISIBLE
        binding.layoutWeather.visibility = View.VISIBLE
        binding.cardTemp.visibility = View.VISIBLE
        binding.cardPressure.visibility = View.VISIBLE
        binding.cardHumidity.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (binding.layoutSearch.visibility == View.VISIBLE) {
            binding.layoutSearch.visibility = View.GONE
            binding.layoutLocation.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }
}
