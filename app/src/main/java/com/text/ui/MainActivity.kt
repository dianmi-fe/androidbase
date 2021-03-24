package com.text.ui

import android.os.Bundle
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.arnold.common.architecture.base.BaseActivity
import com.arnold.common.sdk.core.RouterHub
import com.arnold.common.service.calendar.service.ICalendarService
import com.arnold.mvvmcomponent.R
import com.eebochina.train.analytics.annotation.ArnoldDataTrackEvent
import kotlinx.android.synthetic.main.activity_main.*

@Route(path = "/app/mian")
class MainActivity : BaseActivity() {

    @Autowired(name = RouterHub.CALENDAR_SERVICE_CALENDARSERVICE)
    @JvmField
    var calendarService: ICalendarService? = null

    @Autowired(name = RouterHub.CALENDAR_SERVICE_CALENDARSERVICE)
    @JvmField
    var calendar1Service: ICalendarService? = null

    @Autowired(name = RouterHub.CALENDAR_SERVICE_CALENDARSERVICE)
    @JvmField
    var calendar2Service: ICalendarService? = null

    override fun enableARouterInject(): Boolean = true

    override fun layout(): Any = R.layout.activity_main

    override fun initView(savedInstanceState: Bundle?) {

        if (calendarService != null) {
            appTvContent.text = "成功加载日历组件"
        } else {
            appTvContent.text = "日历组件加载失败"
        }


//        appTvContent.setOnClickListener {
//            Toast.makeText(this,"点击了",Toast.LENGTH_SHORT).show()
//        }

        appTvContent.setOnClickListener {
            Toast.makeText(
                this@MainActivity,
                "点击了",
                Toast.LENGTH_SHORT
            ).show()

            initTrack()
        }
    }

    override fun initData(savedInstanceState: Bundle?) {


    }



    @ArnoldDataTrackEvent(eventName = "initTrack",properties = "{\"pageRoute\":\"abcd\",\"sessionId\":\"12233\",\"pagePath\":\"com.text.ui.MainActivity\"}")
    fun initTrack() {

    }
}
