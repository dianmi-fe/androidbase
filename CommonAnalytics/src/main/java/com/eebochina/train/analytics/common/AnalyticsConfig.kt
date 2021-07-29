package com.eebochina.train.analytics.common

object AnalyticsConfig {

    const val TYPE_ACTIVITY_CREATE: Int = 1 shl 1
    const val TYPE_ACTIVITY_RESUME: Int = 1 shl 2
    const val TYPE_ACTIVITY_PAUSED: Int = 1 shl 3

    const val TYPE_FRAGMENT_CREATE: Int = 2 shl 1
    const val TYPE_FRAGMENT_RESUME: Int = 2 shl 2
    const val TYPE_FRAGMENT_PAUSED: Int = 2 shl 3


    /**进入页面*/
    const val TYPE_START = 1

    /**进入前台*/
    const val TYPE_RESUME = 4

    /**退到后台*/
    const val TYPE_PAUSE = 5

    /**接口数据*/
    const val TYPE_API_INFO = 8

    /**页面接口错误*/
    const val TYPE_API_ERROR = 10

    /**自定义事件*/
    const val TYPE_EVENT = 11
}