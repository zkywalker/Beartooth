package org.zky.beartooth.debug

import org.zky.beartooth.BtTask

/**
 * Created by zhangkun on 2020/4/10 Friday.
 */

object L : ILog {

    private var log: ILog? = null

    /**
     * 设置日志等级：
     * -1 不打印 0 仅打印data相关 1 打印e以上  2 打印i以上  3 打印全部
     */
    var logLevel: Int = 3

    fun setDelegate(log: ILog) {
        this.log = log
    }

    fun data(task: BtTask<*>, tag: String, msg: String) {
        if (logLevel < 0) {
            return
        }
        log?.i(tag, "task:${task.mDeviceAddress}/$msg")
    }

    override fun v(tag: String, msg: String) {
        if (logLevel < 3) {
            return
        }
        log?.v(tag, msg)
    }

    override fun i(tag: String, msg: String) {
        if (logLevel < 2) {
            return
        }
        log?.i(tag, msg)
    }

    override fun e(tag: String, msg: String, e: Throwable) {
        if (logLevel < 1) {
            return
        }
        log?.e(tag, msg, e)
    }
}

interface ILog {
    fun v(tag: String, msg: String)
    fun i(tag: String, msg: String)
    fun e(tag: String, msg: String, e: Throwable)
}
