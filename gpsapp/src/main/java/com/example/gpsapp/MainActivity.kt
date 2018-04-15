package com.example.gpsapp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : CheckPermissionsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.requestLocationUpdates("gps", 1000, 2f, MyLocationListener())

    }

    inner class MyLocationListener : LocationListener {

        override fun onLocationChanged(location: Location?) {
            println(location)
            txt_main_location.text = "经度：${location?.longitude}--维度：${location?.latitude}"
        }

        //位置提供者的状态
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        // 当前位置提供者可用
        override fun onProviderEnabled(provider: String?) {
        }

        // 当前位置提供者不可用
        override fun onProviderDisabled(provider: String?) {
        }

    }
}


