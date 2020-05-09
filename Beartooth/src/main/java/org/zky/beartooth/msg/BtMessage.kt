package org.zky.beartooth.msg

import org.zky.beartooth.Beartooth
import org.zky.beartooth.utils.getType
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * Created by zhangkun on 2020/4/9 Thursday.
 */
abstract class BtMessage<Response, Request>(val msg: Request, val isNeedResponse: Boolean) {

    var responseType: Type? = null
        get() {
            if (field == null) {
                field = getType(javaClass.genericSuperclass!!)
            }
            return field
        }

    var sendTS = 0L

    var delayMs = 0L

    open var readTimeOut = 2000L

    var retryCount = 0

    abstract fun writeData(writer: OutputStream)

    /**
     * 处理超时
     */
    open fun handleTimeout() {}

    /**
     * 阻塞式的消息自己处理回调（非阻塞的消息通过统一的回调处理消息，
     * 因为只有一条io流，达不到消息的点对点响应）
     */
    internal fun handleData(data: ByteArray): Boolean =
        onReceiveData(Beartooth.convert(responseType, data))

    /**
     * 当为阻塞消息时候，需要自己处理消息内容
     * @return 是否要自己处理消息，返回false会继续等待返回值
     */
    open fun onReceiveData(data: Response): Boolean = true

    operator fun plus(delay: Long): BtMessage<*, *> {
        delayMs += delay
        return this
    }
}