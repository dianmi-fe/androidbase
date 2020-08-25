package com.arnold.common.mvp.integration.lifecycle

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.trello.rxlifecycle4.android.FragmentEvent
import io.reactivex.rxjava3.subjects.Subject

/**
 * 配合 [FragmentLifecycleable] 使用,使 [Fragment] 具有 [RxLifecycle] 的特性
 */
class FragmentLifecycleForRxLifecycle : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        if (f is FragmentLifecycleable) {
            obtainSubject(f).onNext(FragmentEvent.ATTACH)
        }
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        if (f is FragmentLifecycleable) {
            obtainSubject(f).onNext(FragmentEvent.CREATE)
        }
    }

    override fun onFragmentActivityCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        if (f is FragmentLifecycleable) {
            obtainSubject(f).onNext(FragmentEvent.CREATE_VIEW)
        }
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        if (f is FragmentLifecycleable) {
            obtainSubject(f).onNext(FragmentEvent.START)
        }
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        if (f is FragmentLifecycleable) {
            obtainSubject(f).onNext(FragmentEvent.RESUME)
        }
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        if (f is FragmentLifecycleable) {
            obtainSubject(f).onNext(FragmentEvent.PAUSE)
        }
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        if (f is FragmentLifecycleable) {
            obtainSubject(f).onNext(FragmentEvent.STOP)
        }
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        if (f is FragmentLifecycleable) {
            obtainSubject(f).onNext(FragmentEvent.DESTROY_VIEW)
        }
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        if (f is FragmentLifecycleable) {
            obtainSubject(f).onNext(FragmentEvent.DESTROY)
        }
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        if (f is FragmentLifecycleable) {
            obtainSubject(f).onNext(FragmentEvent.DETACH)
        }
    }

    private fun obtainSubject(fragment: Fragment): Subject<FragmentEvent> {
        return (fragment as FragmentLifecycleable).provideLifecycleSubject()
    }
}