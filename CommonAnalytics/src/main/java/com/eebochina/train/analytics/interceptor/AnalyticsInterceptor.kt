package com.eebochina.train.analytics.interceptor

import android.app.Activity
import android.text.TextUtils
import com.eebochina.train.analytics.AnalyticsDataApi
import com.eebochina.train.analytics.base.IAnalytics
import com.eebochina.train.analytics.common.AnalyticsConfig
import com.eebochina.train.analytics.entity.ApiInfoBean

object AnalyticsInterceptor {

    private val apiInfoBeans: MutableList<ApiInfoBean> = mutableListOf()
    private val apiErrorBeans: MutableList<ApiInfoBean> = mutableListOf()
    private var lastActivityPath: String? = null
    private var lastSessionId: String? = null
    private var lastRoute: String? = null

    @Synchronized
    fun intercept(
        url: String,
        sentRequestAtMillis: Long,
        receivedResponseAtMillis: Long,
        requestId: String?,
        accesstoken: String?,
        currentActivity: Activity?,
        status: String,
        isSuccessful: Boolean = true
    ) {

        val currentActivityPath: String? = currentActivity?.javaClass?.canonicalName

        if (currentActivity != null) {
            if (lastActivityPath != null && lastActivityPath != currentActivityPath && apiInfoBeans.size > 0 || apiInfoBeans.size > 15) {
                //上报数据
                if (apiErrorBeans.size > 0) {
                    AnalyticsDataApi.updateApi(
                        lastRoute ?: "",
                        apiErrorBeans.toList(),
                        lastActivityPath,
                        lastSessionId,
                        System.currentTimeMillis(),
                        System.currentTimeMillis(),
                        AnalyticsConfig.TYPE_API_ERROR
                    )
                }

                AnalyticsDataApi.updateApi(
                    lastRoute ?: "",
                    apiInfoBeans.toList(),
                    lastActivityPath,
                    lastSessionId,
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    AnalyticsConfig.TYPE_API_INFO
                )
                apiInfoBeans.clear()
                apiErrorBeans.clear()
            }
            lastActivityPath = currentActivityPath

            if (currentActivity is IAnalytics) {
                lastRoute =
                    if (TextUtils.isEmpty(currentActivity.pageRoute())) "" else "${currentActivity.pageRoute()}"

                lastSessionId =
                    if (TextUtils.isEmpty(currentActivity.sessionId())) "${System.currentTimeMillis()}" else currentActivity.sessionId()
            }
            if (!TextUtils.isEmpty(lastRoute)) {
                apiInfoBeans.add(
                    ApiInfoBean(
                        requestId ?: "",
                        accesstoken ?: "",
                        sentRequestAtMillis,
                        receivedResponseAtMillis,
                        status,
                        url
                    )
                )
                if (!isSuccessful) {
                    apiErrorBeans.add(
                        ApiInfoBean(
                            requestId ?: "",
                            accesstoken ?: "",
                            sentRequestAtMillis,
                            receivedResponseAtMillis,
                            status,
                            url
                        )
                    )
                }
            }
        }

    }


}