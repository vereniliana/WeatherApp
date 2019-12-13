package com.example.weatherapp.view

import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    var latitude = MutableLiveData<String>()
    var longitude =  MutableLiveData<String>()

    @Bindable
    fun setLat(latValue : Double) {
        latitude.value = latValue.toString()
    }

    fun setLong(longValue : Double) {
        longitude.value = longValue.toString()
    }
}