package com.arnold.common.mvp.integration.lifecycle

import io.reactivex.Observable


/**
 *  * 让 [Activity]/[Fragment] 实现此接口,即可正常使用 [RxLifecycle]
 * 无需再继承 [RxLifecycle] 提供的 Activity/Fragment ,扩展性极强
 */
interface Lifecycleable<T> {

    fun provideLifecycleSubject(): Observable<T>
}