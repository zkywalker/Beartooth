package org.zky.beartooth.msg

import java.io.OutputStream

/**
 * Created by zhangkun on 2020/5/7 Thursday.
 */
 open class MultiMessage<Response> : BaseMessages<Response, Any> {

    constructor(
        msg: String,
        isNeedResponse: Boolean = false,
        callback: ((Response?) -> Unit)? = null
    ) : super(msg, isNeedResponse, callback)


    constructor(
        msg: ByteArray,
        isNeedResponse: Boolean = false,
        callback: ((Response?) -> Unit)? = null
    ) : super(msg, isNeedResponse, callback)

    constructor(
        msg: CharArray,
        isNeedResponse: Boolean = false,
        callback: ((Response?) -> Unit)? = null
    ) : super(msg, isNeedResponse, callback)

    override fun writeData(writer: OutputStream) {
        when (msg) {
            is String -> {
                writer.write(msg.toByteArray())
            }
            is ByteArray -> {
                writer.write(msg)
            }
            is CharArray -> {
                writer.write(msg.toString().toByteArray())
            }
        }
        writer.flush()
    }

    override fun checkIsRequireData(data: Response): Boolean = true

}