package com.eebochina.train.analytics.entity

data class ApiInfoBean(
    val x_request_id: String,
    val x_request_uid: String,
    val req_time: Long,
    val res_time: Long,
    val status: String,
    val path: String
)