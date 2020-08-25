package com.arnold.mvvmcomponent.login.model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arnold.common.architecture.di.scope.ActivityScope
import com.arnold.common.mvvm.BaseViewModel
import com.arnold.common.mvvm.app.toResourceLiveData
import com.arnold.common.mvvm.data.Resource
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@ActivityScope
class LoginViewModel
@Inject
constructor(application: Application, loginModel: LoginModel) :
    BaseViewModel<LoginModel>(application, loginModel) {

    private val _loginLiveData = MutableLiveData<Resource<String>>()
    internal val loginLiveData: LiveData<Resource<String>>
        get() = _loginLiveData

    init {
        mModel?.let {
            it.login()
                .subscribeOn(Schedulers.io())
                .subscribe({
                    _loginLiveData.postValue(Resource.success(it))
                }, {
                    _loginLiveData.postValue(Resource.error(it))
                })
        }
    }


    fun login(): LiveData<Resource<String>> {
       return mModel!!.login()
            .subscribeOn(Schedulers.io())
            .toResourceLiveData()

    }
}