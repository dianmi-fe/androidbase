package com.eebochina.train.analytics

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import com.eebochina.train.analytics.base.IAnalytics
import java.text.SimpleDateFormat
import java.util.*

object DataAutoTrackHelper {

    private val dataMap: MutableMap<String, Long> = mutableMapOf()

    private fun isFragment(clazz: Any?): Boolean {
        try {
            if (clazz == null) {
                return false
            }
            var supportFragmentClass: Class<*>? = null
            var androidXFragmentClass: Class<*>? = null
            var fragment: Class<*>? = null
            try {
                fragment = Class.forName("android.app.Fragment")
            } catch (e: Exception) {
                //ignored
            }
            try {
                supportFragmentClass = Class.forName("android.support.v4.app.Fragment")
            } catch (e: Exception) {
                //ignored
            }
            try {
                androidXFragmentClass = Class.forName("androidx.fragment.app.Fragment")
            } catch (e: Exception) {
                //ignored
            }
            if (supportFragmentClass == null && androidXFragmentClass == null && fragment == null) {
                return false
            }
            if (supportFragmentClass != null && supportFragmentClass.isInstance(clazz) ||
                androidXFragmentClass != null && androidXFragmentClass.isInstance(clazz) ||
                fragment != null && fragment.isInstance(clazz)
            ) {
                return true
            }
        } catch (e: Exception) {
            //ignored
        }
        return false
    }

    fun trackFragmentSetUserVisibleHint(
        clazz: Any,
        isVisibleToUser: Boolean
    ) {

        if (!isFragment(clazz) || clazz !is IAnalytics) {
            return
        }
        var parentFragment: Any? = null
        try {
            val getParentFragmentMethod = clazz.javaClass.getMethod("getParentFragment")
            parentFragment = getParentFragmentMethod.invoke(clazz)
        } catch (e: java.lang.Exception) {
            //ignored
        }
        if (parentFragment == null) {
            if (isVisibleToUser) {
                if (fragmentIsResumed(clazz)) {
                    if (!fragmentIsHidden(clazz)) {
                        trackFragmentAppViewScreen(clazz, true)
                    }
                }
            }
        } else {
            if (isVisibleToUser && fragmentGetUserVisibleHint(parentFragment)) {
                if (fragmentIsResumed(clazz) && fragmentIsResumed(parentFragment)) {
                    if (!fragmentIsHidden(clazz) && !fragmentIsHidden(parentFragment)) {
                        trackFragmentAppViewScreen(clazz, true)
                    }
                }
            }
        }
    }

    fun trackFragmentResume(clazz: Any) {

        if (!isFragment(clazz) || clazz !is IAnalytics) {
            return
        }
        try {
            val getParentFragmentMethod =
                clazz.javaClass.getMethod("getParentFragment")
            val parentFragment = getParentFragmentMethod.invoke(clazz)
            if (parentFragment == null) {
                if (!fragmentIsHidden(clazz) && fragmentGetUserVisibleHint(clazz)) {
                    trackFragmentAppViewScreen(clazz, true)
                }
            } else {
                if (!fragmentIsHidden(clazz) && fragmentGetUserVisibleHint(clazz) && !fragmentIsHidden(
                        parentFragment
                    ) && fragmentGetUserVisibleHint(parentFragment)
                ) {
                    trackFragmentAppViewScreen(clazz, true)
                }
            }
        } catch (e: java.lang.Exception) {
            //ignored
        }
    }

    fun trackFragmentPaused(clazz: Any) {
        if (!isFragment(clazz) || clazz !is IAnalytics) {
            return
        }
        try {
            trackFragmentAppViewScreen(clazz, false)
        } catch (e: java.lang.Exception) {
            //ignored
        }
    }

    fun trackOnHiddenChanged(clazz: Any, hidden: Boolean) {

        if (!isFragment(clazz) || clazz !is IAnalytics) {
            return
        }
        var parentFragment: Any? = null
        try {
            val getParentFragmentMethod = clazz.javaClass.getMethod("getParentFragment")
            parentFragment = getParentFragmentMethod.invoke(clazz)
        } catch (e: java.lang.Exception) {
            //ignored
        }
        if (parentFragment == null) {
            if (!hidden) {
                if (fragmentIsResumed(clazz)) {
                    if (fragmentGetUserVisibleHint(clazz)) {
                        trackFragmentAppViewScreen(clazz, false)
                    }
                }
            }
        } else {
            if (!hidden && !fragmentIsHidden(parentFragment)
            ) {
                if (fragmentIsResumed(clazz) && fragmentIsResumed(parentFragment)
                ) {
                    if (fragmentGetUserVisibleHint(clazz) && fragmentGetUserVisibleHint(
                            parentFragment
                        )
                    ) {
                        trackFragmentAppViewScreen(clazz, false)
                    }
                }
            }
        }
    }


    private fun trackFragmentAppViewScreen(fragment: IAnalytics, isShow: Boolean) {
        if ("com.bumptech.glide.manager.SupportRequestManagerFragment" == fragment.javaClass.canonicalName ||
            "com.gyf.immersionbar.SupportRequestManagerFragment" == fragment.javaClass.canonicalName ||
            "com.gyf.immersionbar.RequestManagerFragment" == fragment.javaClass.canonicalName
        ) {
            return
        }

        if (TextUtils.isEmpty(fragment.pageRoute())) {
            return
        }

        var stayTime: Long? = null
        val time = System.currentTimeMillis()
        if (isShow) {
            dataMap["${fragment.pageRoute()}visit"] = time
        } else {
            val startTime = dataMap.remove("${fragment.pageRoute()}visit")
            if (startTime != null) {
                stayTime = time - startTime
            }
        }

        if (AnalyticsDataApi.debug) {
            Log.i(
                "Analytics",
                "行为类型:${if (isShow) "进入页面" else "离开页面"}， 进入时间:${HMS(time)},停留时间:${
                    if (stayTime != null) HMS(
                        stayTime
                    ) else "无"
                }"
            )
        }



        AnalyticsDataApi.updateData(
            if (isShow) AnalyticsDataApi.TYPE_START else AnalyticsDataApi.TYPE_END,
            fragment.javaClass.canonicalName ?: "",
            "${fragment.pageRoute()}visit",
            stayTime?.toString() ?: "",
            fragment.parameter()
        )
    }

    fun trackActivityAppViewScreen(page: String, data: IAnalytics, isShow: Boolean) {
        if (TextUtils.isEmpty(data.pageRoute())) {
            return
        }
        var stayTime: Long? = null
        val time = System.currentTimeMillis()
        if (isShow) {
            dataMap["${data.pageRoute()}visit"] = time
        } else {
            val startTime = dataMap.remove("${data.pageRoute()}visit")
            if (startTime != null) {
                stayTime = time - startTime
            }
        }

        if (AnalyticsDataApi.debug) {
            Log.i(
                "Analytics",
                "行为类型:${if (isShow) "进入页面" else "离开页面"}， 进入时间:${HMS(time)},停留时间:${
                    if (stayTime != null) HMS(
                        stayTime
                    ) else "无"
                }"
            )
        }

        AnalyticsDataApi.updateData(
            if (isShow) AnalyticsDataApi.TYPE_START else AnalyticsDataApi.TYPE_END,
            page, "${data.pageRoute()}visit", stayTime?.toString() ?: "", data.parameter()
        )
    }

    /**自定义事件*/
    fun trackEvent(route: String, data: Map<String, Any?>?, pagePath: String? = null) {
        if (TextUtils.isEmpty(route)) {
            return
        }
        AnalyticsDataApi.updateData(
            AnalyticsDataApi.TYPE_EVENT,
            pagePath, route, null, data
        )
    }


    fun trackApiError(
        route: String, data: Map<String, Any?>?
    ) {

        if (TextUtils.isEmpty(route)) {
            return
        }
        AnalyticsDataApi.updateData(
            AnalyticsDataApi.TYPE_API_ERROR,
            null, route, null, data
        )

    }

    private fun fragmentIsResumed(fragment: Any): Boolean {
        try {
            val isResumedMethod =
                fragment.javaClass.getMethod("isResumed")
            return isResumedMethod.invoke(fragment) as Boolean
        } catch (e: java.lang.Exception) {
            //ignored
        }
        return false
    }

    private fun fragmentIsHidden(fragment: Any): Boolean {
        try {
            val isHiddenMethod = fragment.javaClass.getMethod("isHidden")
            return isHiddenMethod.invoke(fragment) as Boolean
        } catch (e: java.lang.Exception) {
            //ignored
        }
        return false
    }

    private fun fragmentGetUserVisibleHint(fragment: Any): Boolean {
        try {
            val getUserVisibleHintMethod =
                fragment.javaClass.getMethod("getUserVisibleHint")
            return getUserVisibleHintMethod.invoke(fragment) as Boolean
        } catch (e: java.lang.Exception) {
            //ignored
        }
        return false
    }

    @SuppressLint("SimpleDateFormat")
    private fun HMS(time: Long): String? {
        val sdr = SimpleDateFormat("HH:mm:ss")
        return sdr.format(Date(time))
    }

}