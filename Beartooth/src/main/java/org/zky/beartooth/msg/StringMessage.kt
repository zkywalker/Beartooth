package org.zky.beartooth.msg

import java.io.OutputStream
import java.nio.charset.Charset

/**
 * 字符串消息
 * Created by zhangkun on 2020/5/6 Wednesday.
 */
abstract class StringMessage<Response>(
    msg: String,
    val charset: Charset = Charsets.UTF_8,
    isNeedResponse: Boolean,
    callback: ((Response?) -> Unit)? = null
) : BaseMessages<Response, String>(msg, isNeedResponse, callback) {
    override fun writeData(writer: OutputStream) {
        writer.write(msg.toByteArray(charset))
        writer.flush()
    }
}

class SimpleMessage(
    msg: String,
    isNeedResponse: Boolean = false,
    callback: ((ByteArray?) -> Unit)? = null
) : StringMessage<ByteArray>(msg, isNeedResponse = isNeedResponse, callback = callback) {

    override fun checkIsRequireData(data: ByteArray): Boolean = true

}