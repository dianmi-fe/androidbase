package com.eebochina.train.analytics.config

class AConfigOptions {

    /**服务器地址*/
    var serviceUrl: String = ""

    /**调试模式*/
    var debug: Boolean = false

    /**项目*/
    var project: String = ""

    var onGetCompanyNo: (() -> String)? = null

    /**企业ID*/
    val companyNo: String
        get() = onGetCompanyNo?.invoke() ?: ""

    /**分辨率*/
    var resolutionRatio: String? = ""

    /**唯一标识符*/
    var identifier: String = ""

    var onGetToken: (() -> String)? = null

    /**token*/
    val token: String
        get() = onGetToken?.invoke() ?: ""

    var onGetMobile: (() -> String)? = null

    /**手机号*/
    val mobile: String
        get() = onGetMobile?.invoke() ?: ""

    var onGetUserId: (() -> String)? = null

    /**用户ID*/
    val userId: String
        get() = onGetUserId?.invoke() ?: ""

    var onGetCommonParameters: (() -> Map<String, Any>)? = null

    /**功能参数*/
    val commonParameters: Map<String, Any>
        get() = onGetCommonParameters?.invoke() ?: mapOf()

}