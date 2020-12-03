package com.eebochina.train.analytics.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ApiAnalytics(
    /**行为类型*/
    var bt: Int,

    /**路由*/
    var route: String,
    var t: Long,

    /**产品*/
    var pd: String,
    /**屏幕分辨率*/
    var ds: String?,

    /**设备*/
    var os: String,

    /**每次登录的唯一标识（使用token）*/
    var token: String,
    var u: String?,
    var key: String?,

    /**额外参数 json*/
    var params: String?,

    /**企业识别码*/
    var cn: String?,

    /**子账号account*/
    var ac: String?,
    /**网络请求 json 数据*/
    var api: String?

) {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0


}