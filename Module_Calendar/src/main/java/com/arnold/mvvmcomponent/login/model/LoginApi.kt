package com.arnold.mvvmcomponent.login.model

import io.reactivex.Observable
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginApi {
    @FormUrlEncoded
    @POST("patriarch/ruleurl/baseinfo")
    fun getRuleUrlBaseInfo(): Observable<String>
}