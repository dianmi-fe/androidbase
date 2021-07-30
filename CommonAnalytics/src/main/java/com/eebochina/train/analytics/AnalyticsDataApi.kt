package com.eebochina.train.analytics

import android.app.Application
import android.text.TextUtils
import android.util.Log
import com.eebochina.train.analytics.common.AnalyticsConfig
import com.eebochina.train.analytics.config.AConfigOptions
import com.eebochina.train.analytics.core.ActivityLifecycleCallbacksImpl
import com.eebochina.train.analytics.http.HttpUtil
import com.eebochina.train.analytics.util.AppMonitor
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

object AnalyticsDataApi {

    private var aConfigOptions: AConfigOptions? = null

    var debug: Boolean = true

    /* SDK 配置是否初始化 */
    private var mSDKConfigInit = true

    //上一个页面路径
    private var lastPagePath: String? = null


    public var sessionId: String = ""
    private var startTime: Long = 0
    private var stopTime: Long = 0


    fun startWithConfigOptions(application: Application, aConfigOptions: AConfigOptions) {
        this.aConfigOptions = aConfigOptions
        if (mSDKConfigInit) {
            mSDKConfigInit = false
            debug = aConfigOptions.debug
            HttpUtil.isDebug = aConfigOptions.debug

            application.registerActivityLifecycleCallbacks(ActivityLifecycleCallbacksImpl())
            AppMonitor.get().initialize(application)
            AppMonitor.get().register(object : AppMonitor.Callback {
                override fun onAppForeground() {
                    sessionId = UUID.randomUUID().toString().replace("-", "");
                    startTime = System.currentTimeMillis()
                    stopTime = startTime
                    updateData(4, null, "", "", startTime, stopTime, null)
                }

                override fun onAppBackground() {
                    stopTime = System.currentTimeMillis()
                    updateData(5, null, "", "", startTime, stopTime, null)
                }

                override fun onAppUIDestroyed() {

                }
            })
        }
    }


    /**上传api数据*/
    fun updateApi(
        route: String,
        count: Int,
        errorCount: Int,
        pagePath: String? = null,
        startTime: Long?,
        endTime: Long?,
        bt: Int = AnalyticsConfig.TYPE_API_INFO
    ) {

        if (TextUtils.isEmpty(aConfigOptions?.serviceUrl)) {
            Log.e("Analytics", "上报地址不能为空")
            return
        }
        val parameter = mutableMapOf<String, Any>().apply {
            put("bd", mutableMapOf<String, Any>().apply {
                /**行为数据*/
                put("key", "")

                put("params", mutableMapOf<String, Any>().apply {
                    if (aConfigOptions?.commonParameters != null && aConfigOptions!!.commonParameters.isNotEmpty()) {
                        putAll(aConfigOptions!!.commonParameters)
                    }
                })
            })
//            put("comp_info", mutableMapOf<String, Any>().apply {
//                put("cn", aConfigOptions?.companyNo ?: "")
//                put("ac", aConfigOptions?.mobile ?: "")
//            })
            /**每次登录的唯一标识*/
            put("session_id", sessionId)
            /**行为类型*/
            put("bt", bt)
            /**用户id*/
//            put("ei", aConfigOptions?.userId ?: "")
            /**时间戳*/
//            put("t", System.currentTimeMillis())
            /**产品*/
            put("type", aConfigOptions?.project ?: "1")
            /**屏幕分辨率*/
//            put("ds", aConfigOptions?.resolutionRatio ?: "1080x1920")
            /**设备*/
//            put("os", "2")
            /**路由*/
            put("sc", route)
            put("api", mutableMapOf<String, Int>().apply {
                put("count", count)
                put("error_count", errorCount)
            })
            startTime?.let {
                put("st", startTime)
            }
            endTime?.let {
                put("et", endTime)
            }
            pagePath?.let {
                /**当前页面url（web）、页面路径（小程序）*/
                put("u", pagePath)
            }

        }
        GlobalScope.launch(Dispatchers.IO) {
            try {
                HttpUtil.post(
                    aConfigOptions?.serviceUrl!!,
                    Gson().toJson(parameter),
                    mutableMapOf<String, String>().apply {
                        put("token_id", aConfigOptions?.token ?: "")
                    }
                )
            } catch (e: Exception) {
            }
        }

    }


    /**
     * 上传数据
     * bt：行为类型 （1: 进入页面2: 离开页面3:事件4: 滚动5: 鼠标滑动6: 分享7.搜索,8: api,10:浏览出错）
     * pagePath：页面路径
     * bd：离开页面时的停留时间等
     * t：时间戳
     */
    fun updateData(
        bt: Int,
        pagePath: String?,
        route: String,
        key: String?,
        startTime: Long?,
        endTime: Long?,
        otherParameters: Map<String, Any?>?
    ) {
        if (TextUtils.isEmpty(aConfigOptions?.serviceUrl)) {
            Log.e("Analytics", "上报地址不能为空")
            return
        }

        val params = mutableMapOf<String, Any?>().apply {
            if (otherParameters != null && otherParameters.isNotEmpty()) {
                putAll(otherParameters)
            }
            if (aConfigOptions?.commonParameters != null && aConfigOptions!!.commonParameters.isNotEmpty()) {
                putAll(aConfigOptions!!.commonParameters)
            }
        }

        val parameter = mutableMapOf<String, Any>().apply {
            put("bd", mutableMapOf<String, Any>().apply {
                /**行为数据*/
                put("key", key ?: "")
                put("params", params)
            })

            /**会话id*/
            put("session_id", sessionId)
            /**行为类型*/
            put("bt", "$bt")
            /**用户id*/
//            put("ei", aConfigOptions?.userId ?: "")
            /**时间戳*/
//            put("t", System.currentTimeMillis())
            /**产品*/
            put("type", aConfigOptions?.project ?: "1")
            /**屏幕分辨率*/
//            put("ds", aConfigOptions?.resolutionRatio ?: "1080x1920")
            /**设备*/
//            put("os", "2")
            /**路由*/
            put("sc", route)

            if (bt == 1) {
                lastPagePath?.let {
                    put("rf", it)
                }
            }

            startTime?.let {
                put("st", startTime)
            }
            endTime?.let {
                put("et", endTime)
            }
            pagePath?.let {
                /**当前页面url（web）、页面路径（小程序）*/
                put("u", pagePath)
                lastPagePath = it
            }

        }
        GlobalScope.launch(Dispatchers.IO) {
            try {
                HttpUtil.post(
                    aConfigOptions?.serviceUrl!!,
                    Gson().toJson(parameter),
                    mutableMapOf<String, String>().apply {
                        put("token_id", aConfigOptions?.token ?: "")
                    }
                )
            } catch (e: Exception) {
            }
        }
    }

    /**
     * 上传数据
     * bt：行为类型 （1: 进入页面2: 离开页面3:事件4: 滚动5: 鼠标滑动6: 分享7.搜索,8: api,10:浏览出错）
     * pagePath：页面路径
     * bd：离开页面时的停留时间等
     * sec:事件编码（2号人事新增)
     * t：时间戳
     */
    fun updateData(
        bt: Int,
        sec: String?,
        pagePath: String?,
        route: String,
        key: String?,
        startTime: Long?,
        endTime: Long?,
        otherParameters: Map<String, Any?>?
    ) {
        if (TextUtils.isEmpty(aConfigOptions?.serviceUrl)) {
            Log.e("Analytics", "上报地址不能为空")
            return
        }

        val params = mutableMapOf<String, Any?>().apply {
            if (otherParameters != null && otherParameters.isNotEmpty()) {
                putAll(otherParameters)
            }
            if (aConfigOptions?.commonParameters != null && aConfigOptions!!.commonParameters.isNotEmpty()) {
                putAll(aConfigOptions!!.commonParameters)
            }
        }

        val parameter = mutableMapOf<String, Any>().apply {
            put("bd", mutableMapOf<String, Any>().apply {
                /**行为数据*/
                put("key", key ?: "")
                put("params", params)
            })

            /**会话id*/
            put("session_id", sessionId)
            /**行为类型*/
            put("bt", "$bt")
            /**用户id*/
//            put("ei", aConfigOptions?.userId ?: "")
            /**时间戳*/
//            put("t", System.currentTimeMillis())
            /**产品*/
            put("type", aConfigOptions?.project ?: "1")
            /**屏幕分辨率*/
//            put("ds", aConfigOptions?.resolutionRatio ?: "1080x1920")
            /**设备*/
//            put("os", "2")
            /**路由*/
            put("sc", route)

            if (bt == 1) {
                lastPagePath?.let {
                    put("rf", it)
                }
            }

            startTime?.let {
                put("st", startTime)
            }
            endTime?.let {
                put("et", endTime)
            }
            //2号人事部新增：sec事件编码
            sec?.let {
                put("sec", sec)
            }
            pagePath?.let {
                /**当前页面url（web）、页面路径（小程序）*/
                put("u", pagePath)
                lastPagePath = it
            }

        }
        GlobalScope.launch(Dispatchers.IO) {
            try {
                HttpUtil.post(
                    aConfigOptions?.serviceUrl!!,
                    Gson().toJson(parameter),
                    mutableMapOf<String, String>().apply {
                        put("token_id", aConfigOptions?.token ?: "")
                    }
                )
            } catch (e: Exception) {
            }
        }
    }
}