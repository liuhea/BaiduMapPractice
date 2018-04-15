package com.example.baidumap

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.baidu.mapapi.map.*
import com.baidu.mapapi.search.poi.*
import kotlinx.android.synthetic.main.activity_main.*
import java.net.HttpURLConnection
import java.net.URL
import android.content.pm.PackageManager
import android.graphics.Color
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.TextView
import android.widget.Toast
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.route.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.ObjectOutputStream
import com.baidu.mapapi.search.route.PlanNode.withCityNameAndPlaceName


class MainActivity : AppCompatActivity() {

    private var poiSearch: PoiSearch = PoiSearch.newInstance()

    lateinit var baiduMap: BaiduMap
    var isFirstLoc = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        baiduMap = bmapView.map
        initBaiduSetting()
        doAuth()
//        initPOI()
        initLocation()
//        getDemo()

        mockAddView()

        mockRouter()

    }

    private fun mockRouter() {
        val stNode = PlanNode.withCityNameAndPlaceName("北京", "西二旗地铁站")
        val enNode = PlanNode.withCityNameAndPlaceName("北京", "百度科技园")

        var routeSearch = RoutePlanSearch.newInstance()
        routeSearch.setOnGetRoutePlanResultListener(object : OnGetRoutePlanResultListener {
            override fun onGetIndoorRouteResult(p0: IndoorRouteResult?) {
            }

            override fun onGetTransitRouteResult(p0: TransitRouteResult?) {
            }

            override fun onGetDrivingRouteResult(p0: DrivingRouteResult?) {
            }

            override fun onGetWalkingRouteResult(p0: WalkingRouteResult?) {

            }

            override fun onGetMassTransitRouteResult(p0: MassTransitRouteResult?) {
            }

            override fun onGetBikingRouteResult(p0: BikingRouteResult?) {

            }
        })
        btn_main_poi.setOnClickListener({
            routeSearch.walkingSearch(WalkingRoutePlanOption()
                    .from(stNode)
                    .to(enNode))
        })
    }

    private fun mockAddView() {

        val textView = TextView(this)
        textView.text = "haha"
        textView.setTextColor(Color.RED)
        textView.textSize = 29f
        bmapView.addView(textView)
    }

    private fun initBaiduSetting() {

        bmapView.logoPosition = LogoPosition.logoPostionRightTop


    }

    private fun doAuth() {
        //检查权限
        val checkSelfPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        //拒绝
        if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
            //申请权限
            ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        } else if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {//已经授权
            Toast.makeText(this, "开始定位", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (100 == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "已经授权成功了", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "未授权定位权限，请设置中打开", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 定位
     */
    private fun initLocation() {
        var mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_location)
        baiduMap.isMyLocationEnabled = true
        var mLocClient = LocationClient(this)
        mLocClient.registerLocationListener(object : BDAbstractLocationListener() {
            override fun onReceiveLocation(location: BDLocation?) {
                // map view 销毁后不在处理新接收的位置
                if (location == null || bmapView == null) {
                    return
                }
                var locData = MyLocationData.Builder()
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100.toFloat()).latitude(location.latitude)
                        .longitude(location.longitude).build()
                baiduMap.setMyLocationData(locData)

                println(location)
                var mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS
                baiduMap.setMyLocationConfiguration(MyLocationConfiguration(
                        mCurrentMode, true, mCurrentMarker))
            }
        })
        val option = LocationClientOption()
        option.isOpenGps = true // 打开gps
        mLocClient.locOption = option
        mLocClient.start()

    }

    /**
     * poi
     */
    private fun initPOI() {
        //[1] 新建poi对象
        //[2]设置poi监听
        val poiListener = object : OnGetPoiSearchResultListener {
            override fun onGetPoiIndoorResult(p0: PoiIndoorResult?) {
                println(p0.toString())
            }

            override fun onGetPoiResult(result: PoiResult) {
                //获取POI检索结果
                println(result)

                if (result?.error == SearchResult.ERRORNO.NO_ERROR) {
                    baiduMap.clear()

                    //[1]获取经纬度location
                    //[2]构建Marker图标
                    val bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_logo)
                    result.allPoi?.mapNotNull {
                        //[3]构建MarkerOption，用于在地图上添加Marker
                        val option = MarkerOptions()
                                .position(it.location)
                                .icon(bitmap) as OverlayOptions
                        //[4]在地图上添加Marker，并显示
                        baiduMap.addOverlay(option)
                    }
                }
            }

            override fun onGetPoiDetailResult(result: PoiDetailResult) {

            }
        }

        poiSearch?.apply {
            setOnGetPoiSearchResultListener(poiListener)
        }
        btn_main_poi.setOnClickListener({

            //[3] 发起poi
            poiSearch.searchInCity(PoiCitySearchOption().city("北京").keyword("美食").pageNum(10))
        })
    }


    fun postDemo(name: String, age: String) {
        Thread(Runnable {
            //[1]创建网络链接实例
            var url = URL("http://")
            val conn = url.openConnection() as HttpURLConnection
            //[2]设置请求方式
            conn.requestMethod = "POST"
            conn.connectTimeout = 5000
            //[3]设置http请求数据的类型为表单类型
            conn.setRequestProperty("Content-Type", "multipart/form-data")
            //[4]给服务器写的数据的长度
            var data = "name=$name&age=$age"
            conn.setRequestProperty("Content-Length", data.length.toString())
            //[5]指定要给服务器写数据
            conn.doOutput = true
            //[6]开始向服务器写数据
            conn.outputStream.write(data.toByteArray())
            //[7]响应数据
            val responseCode = conn.responseCode
            if (HttpURLConnection.HTTP_OK == responseCode) {
                // doSomething
            } else {
                conn.disconnect()
            }
        }).start()
    }

    fun getDemo() {
        Thread(Runnable {
            var conn = URL("http://www.12306.cn/mormhweb/")
                    .openConnection() as HttpURLConnection

            conn.connect()

            println(getReturn(conn))
            println(conn.responseCode)
            println("======" + conn.responseMessage)
        }).start()
    }

    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        bmapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        bmapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        bmapView.onDestroy()
        poiSearch?.destroy()

    }

    /*请求url获取返回的内容*/
    fun getReturn(connection: HttpURLConnection): String {
        val buffer = StringBuffer()
        //将返回的输入流转换成字符串
        connection.inputStream.use { inputStream ->
            InputStreamReader(inputStream, "UTF-8").use({ inputStreamReader ->
                BufferedReader(inputStreamReader).use({ bufferedReader ->
                    var str: String? = null
                    while ((bufferedReader.readLine()) != null) {
                        buffer.append(bufferedReader.readLine())
                    }
                    return buffer.toString()
                })
            })
        }
    }

}
