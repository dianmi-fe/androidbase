package com.arnold.common.mvp.integration.lifecycle

import com.trello.rxlifecycle4.android.ActivityEvent


/**
 * [Activity]实现此接口，即可正常使用[RxLifecycle]
 */
interface ActivityLifecycleable : Lifecycleable<ActivityEvent> {
}