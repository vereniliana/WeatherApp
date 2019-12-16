package com.example.weatherapp.data

import android.content.Context
import android.content.SharedPreferences

class AppPrefHelper(val context: Context, val prefFileName: String) {

    private val mPrefs: SharedPreferences = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE)

    fun getLocation(): String? {
        return mPrefs.getString("location", null)
    }

    fun setLocation(loc: String) {
        mPrefs.edit().putString("location", loc).apply()
    }

    fun getImageCode(): String? {
        return mPrefs.getString("image_code", null)
    }

    fun setImageCode(url: String) {
        mPrefs.edit().putString("image_code", url).apply()
    }

    fun getWeather(): String? {
        return mPrefs.getString("weather", null)
    }

    fun setWeather(weather: String) {
        mPrefs.edit().putString("weather", weather).apply()
    }

    fun getDescription(): String? {
        return mPrefs.getString("description", null)
    }

    fun setDescription(desc: String) {
        mPrefs.edit().putString("description", desc).apply()
    }
    fun getTemperature(): String? {
        return mPrefs.getString("temperature", null)
    }

    fun setTemperature(temp: String) {
        mPrefs.edit().putString("temperature", temp).apply()
    }
    fun getPressure(): String? {
        return mPrefs.getString("pressure", null)
    }

    fun setPressure(pressure: String) {
        mPrefs.edit().putString("pressure", pressure).apply()
    }

    fun getHumdity(): String? {
        return mPrefs.getString("humdity", null)
    }

    fun setHumdity(humdity: String) {
        mPrefs.edit().putString("humdity", humdity).apply()
    }
}