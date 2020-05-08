package com.arnold.common.mvp

import android.os.Bundle
import com.arnold.common.architecture.base.BaseActivity
import com.arnold.common.mvp.integration.lifecycle.ActivityLifecycleable
import com.trello.rxlifecycle3.android.ActivityEvent
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

abstract class BaseMvpActivity<P : IPresenter> : BaseActivity(), IView ,
        ActivityLifecycleable {

    private val mLifecycleSubject = BehaviorSubject.create<ActivityEvent>()

    @Inject
    lateinit var presenter: P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData(savedInstanceState)
    }


    override fun provideLifecycleSubject(): Observable<ActivityEvent> {
        return mLifecycleSubject
    }


    override fun onDestroy() {
        super.onDestroy()
        if (::presenter.isInitialized){
            presenter.onDestroy()
        }

    }

}