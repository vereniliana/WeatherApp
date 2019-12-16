package com.example.weatherapp.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.weatherapp.R
import com.example.weatherapp.data.AppPrefHelper
import com.example.weatherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.*


class MainActivity : AppCompatActivity() {

    val MY_PERMISSIONS_REQUEST_LOCATION = 99

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding : ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var prefHelper: AppPrefHelper

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        prefHelper = AppPrefHelper(this, "weather_data")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (isNetworkAvailable()) {
            getLastLocation()
        } else {
            val loc = prefHelper.getLocation()
            val weather = prefHelper.getWeather()
            val desc = prefHelper.getDescription()
            val temp = prefHelper.getTemperature()
            val pressure = prefHelper.getPressure()
            val humidity= prefHelper.getHumdity()
            val imgCode = prefHelper.getImageCode()
            if (loc != null && weather != null && desc != null && temp != null && pressure != null && humidity != null && imgCode != null) {
                setData(loc, weather, desc, temp, pressure, humidity, imgCode)
            } else {
                Toast.makeText(this, "No Internet", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.dataResponse
            .observe(this, Observer { data ->
                if (data != null) {
                    binding.layoutLocation.visibility = View.VISIBLE
                    binding.layoutSearch.visibility = View.GONE

                    val loc = data.location
                    val weather = data.weatherList[0].main
                    val desc = data.weatherList[0].description
                    val temp = data.info.temperature
                    val pressure = data.info.pressure
                    val humidity= data.info.humidity
                    val imgCode = data.weatherList[0].icon
                    setData(loc, weather, desc, temp, pressure, humidity, imgCode)
                    saveData(loc, weather, desc, temp, pressure, humidity, imgCode)
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

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    @SuppressLint("SetTextI18n")
    private fun setData(loc: String, weather: String, desc: String, temp: String, pressure: String, humidity: String, imgCode: String) {
        binding.tvLocation.text = loc
        binding.tvWeather.text = weather
        binding.tvDescription.text = desc
        binding.tvTemperature.text = viewModel.convertKelvinToCelsius(temp).toString() + getString(R.string.unit_temp)
        binding.tvPressure.text = pressure + getString(R.string.unit_pressure)
        binding.tvHumidity.text = humidity + getString(R.string.unit_humidity)
        viewModel.imageUrl.value = "http://openweathermap.org/img/wn/${imgCode}@2x.png"
    }

    private fun saveData(loc: String, weather: String, desc: String, temp: String, pressure: String, humidity: String, imgCode: String) {
        prefHelper.setLocation(loc)
        prefHelper.setWeather(weather)
        prefHelper.setDescription(desc)
        prefHelper.setTemperature(temp)
        prefHelper.setPressure(pressure)
        prefHelper.setHumdity(humidity)
        prefHelper.setImageCode(imgCode)
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
