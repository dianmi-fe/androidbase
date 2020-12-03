package com.eebochina.train.analytics.interceptor

import android.app.Activity
import android.text.TextUtils
import android.util.Log
import com.eebochina.train.analytics.AnalyticsDataApi
import com.eebochina.train.analytics.base.IAnalytics
import com.eebochina.train.analytics.entity.ApiInfoBean

object AnalyticsInterceptor {

    private val apiInfoBeans: MutableList<ApiInfoBean> = mutableListOf()
    private var lastActivityPath: String? = null
    private var lastRoute: String? = null

    @Synchronized
    fun intercept(
        url: String,
        sentRequestAtMillis: Long,
        receivedResponseAtMillis: Long,
        requestId: String?,
        accesstoken: String?,
        currentActivity: Activity?,
        status: String
    ) {

        val currentActivityPath: String? = currentActivity?.javaClass?.canonicalName

        if (currentActivity != null) {
            if (lastActivityPath != null && lastActivityPath != currentActivityPath && apiInfoBeans.size > 0 || apiInfoBeans.size > 15) {
                //上报数据

                val tempApiInfoBeans = mutableListOf<ApiInfoBean>()
                tempApiInfoBeans.addAll(apiInfoBeans)

                val errorSize = tempApiInfoBeans.filter {
                    it.status != "200"
                }.size

                if (errorSize > 0) {
                    //接口报错，上报异常
                    AnalyticsDataApi.updateData(
                        AnalyticsDataApi.TYPE_API_ERROR,
                        null, lastRoute!!, null, null
                    )
                }


                AnalyticsDataApi.updateApi(lastRoute ?: "", apiInfoBeans.toList())
                apiInfoBeans.clear()
            }
            lastActivityPath = currentActivityPath

            if (currentActivity is IAnalytics) {
                lastRoute =
                    if (TextUtils.isEmpty(currentActivity.pageRoute())) "" else "${currentActivity.pageRoute()}visit"
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
            }
        }

    }


}