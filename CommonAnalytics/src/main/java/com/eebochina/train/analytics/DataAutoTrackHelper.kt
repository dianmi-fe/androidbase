package com.eebochina.train.analytics

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.eebochina.train.analytics.base.IAnalytics
import com.eebochina.train.analytics.common.AnalyticsConfig
import com.eebochina.train.analytics.interceptor.AnalyticsInterceptor
import com.eebochina.train.analytics.util.AopUtil
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

    fun onFragmentViewCreated(clazz: Any, rootView: View) {
        if (!isFragment(clazz) || clazz !is IAnalytics) {
            return
        }
        try {
            //Fragment名称
            val fragmentName = clazz.javaClass.name
            rootView.setTag(R.id.arnold_analytics_tag_view_fragment_name, fragmentName)
            if (rootView is ViewGroup) {
                traverseView(fragmentName, rootView)
            }

            //获取所在的 Context
            val context = rootView.context
            //将 Context 转成 Activity
            val activity: Activity = AopUtil.getActivityFromContext(context)
            if (activity != null) {
                val window = activity.window
                window?.decorView?.rootView?.setTag(
                    R.id.arnold_analytics_tag_view_fragment_name,
                    ""
                )
            }
        } catch (e: java.lang.Exception) {
        } finally {
            trackFragmentAppViewScreen(clazz, AnalyticsConfig.TYPE_FRAGMENT_CREATE)
        }
    }

    private fun traverseView(fragmentName: String, root: ViewGroup?) {
        try {
            if (TextUtils.isEmpty(fragmentName) || root == null) {
                return
            }
            val childCount = root.childCount
            for (i in 0 until childCount) {
                val child = root.getChildAt(i)
                child.setTag(R.id.arnold_analytics_tag_view_fragment_name, fragmentName)
                if (child is ViewGroup && !(child is ListView ||
                            child is GridView ||
                            child is Spinner ||
                            child is RadioGroup)
                ) {
                    traverseView(fragmentName, child)
                }
            }
        } catch (e: java.lang.Exception) {
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
                    trackFragmentAppViewScreen(clazz, AnalyticsConfig.TYPE_FRAGMENT_RESUME)
                }
            } else {
                if (!fragmentIsHidden(clazz) && fragmentGetUserVisibleHint(clazz) && !fragmentIsHidden(
                        parentFragment
                    ) && fragmentGetUserVisibleHint(parentFragment)
                ) {
                    trackFragmentAppViewScreen(clazz, AnalyticsConfig.TYPE_FRAGMENT_RESUME)
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
            trackFragmentAppViewScreen(clazz, AnalyticsConfig.TYPE_FRAGMENT_PAUSED)
        } catch (e: java.lang.Exception) {
            //ignored
        }
    }

    private fun trackFragmentAppViewScreen(
        fragment: IAnalytics,
        fragmentState: Int = AnalyticsConfig.TYPE_FRAGMENT_CREATE
    ) {
        if ("com.bumptech.glide.manager.SupportRequestManagerFragment" == fragment.javaClass.canonicalName ||
            "com.gyf.immersionbar.SupportRequestManagerFragment" == fragment.javaClass.canonicalName ||
            "com.gyf.immersionbar.RequestManagerFragment" == fragment.javaClass.canonicalName
        ) {
            return
        }

        if (TextUtils.isEmpty(fragment.pageRoute())) {
            return
        }

//        if (dataMap["${fragment.javaClass.canonicalName}/startTime"] != null && fragmentState == AnalyticsConfig.TYPE_FRAGMENT_RESUME) {
//            //说明当前页面刚经过onViewCreated 事件
//            return
//        }

        var startTime = System.currentTimeMillis()
        val endTime = startTime

        when (fragmentState) {
            AnalyticsConfig.TYPE_FRAGMENT_CREATE -> {
//                dataMap["${fragment.javaClass.canonicalName}/startTime"] = startTime
                AnalyticsInterceptor.tempPagePath = fragment.javaClass.canonicalName ?: ""
                AnalyticsInterceptor.tempPageRoute = fragment.pageRoute()
                return
            }
            AnalyticsConfig.TYPE_FRAGMENT_RESUME -> {
                dataMap["${fragment.javaClass.canonicalName}/startTime"] = startTime
                AnalyticsInterceptor.tempPagePath = fragment.javaClass.canonicalName ?: ""
                AnalyticsInterceptor.tempPageRoute = fragment.pageRoute()
            }
            else -> {
                AnalyticsInterceptor.apiUpdate(
                    fragment.javaClass.canonicalName ?: "",
                    fragment.pageRoute()
                )

                startTime =
                    dataMap.remove("${fragment.javaClass.canonicalName}/startTime") ?: endTime

                //因为不统计页面离开，所以暂时return
                return
            }
        }

//        val bt = when (fragmentState) {
//            AnalyticsConfig.TYPE_FRAGMENT_CREATE -> {
//                AnalyticsConfig.TYPE_START
//            }
//            AnalyticsConfig.TYPE_FRAGMENT_RESUME -> {
//                AnalyticsConfig.TYPE_RESUME
//            }
//            AnalyticsConfig.TYPE_FRAGMENT_PAUSED -> {
//                AnalyticsConfig.TYPE_PAUSE
//            }
//            else -> {
//                AnalyticsConfig.TYPE_PAUSE
//            }
//        }

        AnalyticsDataApi.updateData(
            AnalyticsConfig.TYPE_START,
            fragment.javaClass.canonicalName ?: "",
            fragment.pageRoute(),
            "",
            null,
            null,
            fragment.parameter()
        )
    }

    fun trackActivityAppViewScreen(
        activity: IAnalytics,
        activityState: Int = AnalyticsConfig.TYPE_ACTIVITY_CREATE
    ) {
        if (TextUtils.isEmpty(activity.pageRoute())) {
            return
        }

//        if (dataMap["${activity.javaClass.canonicalName}/startTime"] != null && activityState == AnalyticsConfig.TYPE_ACTIVITY_RESUME) {
//            //说明当前页面刚经过onCreate 事件
//            return
//        }

        var startTime = System.currentTimeMillis()
        val endTime = startTime

        when (activityState) {
            AnalyticsConfig.TYPE_ACTIVITY_CREATE -> {
//                dataMap["${activity.javaClass.canonicalName}/startTime"] = startTime
//                AnalyticsInterceptor.tempPagePath = activity.javaClass.canonicalName ?: ""
//                AnalyticsInterceptor.tempPageRoute = activity.pageRoute()
                return
            }
            AnalyticsConfig.TYPE_ACTIVITY_RESUME -> {
                dataMap["${activity.javaClass.canonicalName}/startTime"] = startTime
                AnalyticsInterceptor.tempPagePath = activity.javaClass.canonicalName ?: ""
                AnalyticsInterceptor.tempPageRoute = activity.pageRoute()
            }
            else -> {
                AnalyticsInterceptor.apiUpdate(
                    activity.javaClass.canonicalName ?: "",
                    activity.pageRoute()
                )

                startTime =
                    dataMap.remove("${activity.javaClass.canonicalName}/startTime") ?: endTime
                //因为不统计页面离开，所以暂时return
                return
            }
        }

//        val bt = when (activityState) {
//            AnalyticsConfig.TYPE_ACTIVITY_CREATE -> {
//                AnalyticsConfig.TYPE_START
//            }
//            AnalyticsConfig.TYPE_ACTIVITY_RESUME -> {
//                AnalyticsConfig.TYPE_RESUME
//            }
//            AnalyticsConfig.TYPE_ACTIVITY_PAUSED -> {
//                AnalyticsConfig.TYPE_PAUSE
//            }
//            else -> {
//                AnalyticsConfig.TYPE_PAUSE
//            }
//        }

        AnalyticsDataApi.updateData(
            AnalyticsConfig.TYPE_START,
            activity.javaClass.canonicalName,
            activity.pageRoute(),
            "",
            null,
            null,
            activity.parameter()
        )
    }

    /**自定义事件*/
    fun trackEvent(
        route: String,
        data: Map<String, Any?>?,
        pagePath: String? = null,
        key: String? = null
    ) {
        if (TextUtils.isEmpty(route)) {
            return
        }
//        val startTime = System.currentTimeMillis()
        AnalyticsDataApi.updateData(
            AnalyticsConfig.TYPE_EVENT,
            pagePath, route, key, null, null, data
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