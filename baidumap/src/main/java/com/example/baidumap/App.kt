package com.example.baidumap

import android.app.Application
import com.baidu.mapapi.SDKInitializer

/**
 * Created by liuhe on 17/03/2018.
 */
class App : Application() {

    lateinit var locationService: LocationService

    override fun onCreate() {
        super.onCreate()
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        locationService = LocationService(applicationContext)

        SDKInitializer.initialize(applicationContext)
    }
}