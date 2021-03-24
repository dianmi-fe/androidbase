package com.eebochina.train.analytics.base

/**
 * 实现此接口，用于匹配上传路径
 */
interface IAnalytics {

    /**路由（匹配名称）*/
    fun pageRoute(): String

    /**回话ID*/
    fun sessionId(): String {
        return "${System.currentTimeMillis()}"
    }

    /**额外参数*/
    fun parameter(): MutableMap<String, Any?>? = null

    /**是否自动统计当前页面*/
    fun autoTrackPage(): Boolean = true

}