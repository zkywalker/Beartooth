package org.zky.beartooth.demo

import android.app.Application
import android.util.Log
import org.zky.beartooth.debug.ILog
import org.zky.beartooth.debug.L

/**
 * Created by zhangkun on 2020/4/16 Thursday.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // 注册日志
        L.logLevel = 1
        L.setDelegate(object : ILog {
            override fun v(tag: String, msg: String) {
                Log.v(tag, msg)
            }

            override fun i(tag: String, msg: String) {
                Log.i(tag, msg)
            }

            override fun e(tag: String, msg: String, e: Throwable) {
                Log.e(tag, msg, e)
            }

        })
    }
}