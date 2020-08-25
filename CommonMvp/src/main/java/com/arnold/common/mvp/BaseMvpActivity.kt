package com.arnold.common.mvp

import android.os.Bundle
import com.arnold.common.architecture.base.BaseActivity
import com.arnold.common.mvp.integration.lifecycle.ActivityLifecycleable
import com.trello.rxlifecycle4.android.ActivityEvent
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
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

    override fun provideLifecycleSubject(): Subject<ActivityEvent> {
        return mLifecycleSubject
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::presenter.isInitialized){
            presenter.onDestroy()
        }

    }

}