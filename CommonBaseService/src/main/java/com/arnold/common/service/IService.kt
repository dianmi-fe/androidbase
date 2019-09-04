package com.arnold.common.service

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.template.IProvider

/**
 * 接口基类
 */
interface IService : IProvider {
    fun getFragment(): Fragment
}