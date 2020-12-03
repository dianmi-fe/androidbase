package com.eebochina.train.analytics.core

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.eebochina.train.analytics.DataAutoTrackHelper
import com.eebochina.train.analytics.base.IAnalytics

class FragmentLifecycleCallbacksImpl : FragmentManager.FragmentLifecycleCallbacks() {

    companion object {
        val TAG = "analytics"
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        super.onFragmentResumed(fm, f)

        if (f is IAnalytics && f.autoTrackPage()) {
            DataAutoTrackHelper.trackFragmentResume(f)
        }

        Log.i(TAG, "onFragmentResumed: ${f.javaClass.canonicalName}")
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        super.onFragmentPaused(fm, f)
        if (f is IAnalytics && f.autoTrackPage()) {
            DataAutoTrackHelper.trackFragmentPaused(f)
        }
        Log.i(TAG, "onFragmentPaused: ${f.javaClass.canonicalName}")
    }

    override fun onFragmentActivityCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        super.onFragmentActivityCreated(fm, f, savedInstanceState)
        Log.i(TAG, "onFragmentActivityCreated: ${f.javaClass.canonicalName}")
    }

}