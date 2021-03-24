package com.eebochina.train.analytics.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.eebochina.train.analytics.DataAutoTrackHelper
import com.eebochina.train.analytics.base.IAnalytics
import com.eebochina.train.analytics.common.AnalyticsConfig

class ActivityLifecycleCallbacksImpl : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                FragmentLifecycleCallbacksImpl(),
                true
            )
        }

        if (activity is IAnalytics && activity.autoTrackPage()) {
            DataAutoTrackHelper.trackActivityAppViewScreen(
                activity,
                AnalyticsConfig.TYPE_ACTIVITY_CREATE
            )
        }

    }

    override fun onActivityResumed(activity: Activity) {
        if (activity is IAnalytics && activity.autoTrackPage()) {
            DataAutoTrackHelper.trackActivityAppViewScreen(
                activity,
                AnalyticsConfig.TYPE_ACTIVITY_RESUME
            )
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity is IAnalytics && activity.autoTrackPage()) {
            DataAutoTrackHelper.trackActivityAppViewScreen(
                activity,
                AnalyticsConfig.TYPE_ACTIVITY_PAUSED
            )
        }
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityStopped(activity: Activity) {

    }
}