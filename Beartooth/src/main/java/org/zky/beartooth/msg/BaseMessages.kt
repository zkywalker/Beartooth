package org.zky.beartooth.msg

import org.zky.beartooth.debug.L

/**
 * 带有回调闭包的消息超类
 */
abstract class BaseMessages<Response, Request>(
    msg: Request,
    isNeedResponse: Boolean,
    val callback: ((Response?) -> Unit)? = null
) :
    BtMessage<Response, Request>(msg, isNeedResponse) {

    override fun onReceiveData(data: Response): Boolean {
        val isRequiredData = checkIsRequireData(data)
        if (isRequiredData) {
            callback?.invoke(data)
        } else {
            L.i("BtMessage", "isRequiredData = false")
        }
        return isRequiredData
    }

    override fun handleTimeout() {
        callback?.invoke(null)
    }

    abstract fun checkIsRequireData(data: Response): Boolean
}