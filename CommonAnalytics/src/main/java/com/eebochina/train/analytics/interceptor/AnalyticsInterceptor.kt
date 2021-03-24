package com.eebochina.train.analytics.interceptor

import com.eebochina.train.analytics.AnalyticsDataApi
import com.eebochina.train.analytics.common.AnalyticsConfig
import com.eebochina.train.analytics.entity.ApiInfoBean

object AnalyticsInterceptor {

    private val apiInfoBeans: MutableList<ApiInfoBean> = mutableListOf()
    private val apiErrorBeans: MutableList<ApiInfoBean> = mutableListOf()

    //临时使用，当请求接口满15条还没上报数据时，使用此变量
    var tempPagePath: String = ""
    var tempPageRoute: String = ""
    var tempSessionId: String = ""

    @Synchronized
    fun intercept(
        url: String,
        sentRequestAtMillis: Long,
        receivedResponseAtMillis: Long,
        requestId: String?,
        accesstoken: String?,
        status: String,
        isSuccessful: Boolean = true
    ) {
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

        if (apiInfoBeans.size > 15) {
            apiUpdate(tempPagePath, tempPageRoute, tempSessionId)
        }
    }

    @Synchronized
    fun apiUpdate(pagePath: String, pageRoute: String, sessionId: String) {
        //上报数据
        if (apiErrorBeans.size > 0) {
            AnalyticsDataApi.updateApi(
                pageRoute ?: "",
                apiErrorBeans.toList(),
                pagePath,
                sessionId,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                AnalyticsConfig.TYPE_API_ERROR
            )
        }
        if (apiInfoBeans.size > 0) {
            AnalyticsDataApi.updateApi(
                pageRoute ?: "",
                apiInfoBeans.toList(),
                pagePath,
                sessionId,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                AnalyticsConfig.TYPE_API_INFO
            )
        }

        apiErrorBeans.clear()
        apiInfoBeans.clear()
    }

}