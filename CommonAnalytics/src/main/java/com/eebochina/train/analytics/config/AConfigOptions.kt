package com.eebochina.train.analytics.config

class AConfigOptions {

    /**服务器地址*/
    var serviceUrl: String = ""

    /**调试模式*/
    var debug: Boolean = false

    /**项目*/
    var project: String = ""


    /**分辨率*/
    var resolutionRatio: String? = ""

    var onGetToken: (() -> String)? = null

    /**token*/
    val token: String
        get() = onGetToken?.invoke() ?: ""

    var onGetCommonParameters: (() -> Map<String, Any>)? = null

    /**功能参数*/
    val commonParameters: Map<String, Any>
        get() = onGetCommonParameters?.invoke() ?: mapOf()

}