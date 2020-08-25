package com.arnold.common.mvp

import com.arnold.common.architecture.base.BaseFragment
import com.arnold.common.mvp.integration.lifecycle.FragmentLifecycleable
import com.trello.rxlifecycle4.android.FragmentEvent
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import javax.inject.Inject


abstract class BaseMvpFragment<P : IPresenter> : BaseFragment(), IView , FragmentLifecycleable {

    private val mLifecycleSubject = BehaviorSubject.create<FragmentEvent>()

    @Inject
    lateinit var presenter: P

    override fun provideLifecycleSubject(): Subject<FragmentEvent> = mLifecycleSubject

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}