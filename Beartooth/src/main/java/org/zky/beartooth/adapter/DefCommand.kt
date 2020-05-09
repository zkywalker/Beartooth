package org.zky.beartooth.adapter

import org.zky.beartooth.utils.ByteUtils

/**
 * Created by zhangkun on 2020/4/22 Wednesday.
 */
open class DefCommand(
    val perFix: Byte,
    val cmd: Byte,
    val dataLength: Int,
    val data: ByteArray,
    val endCode: Byte,
    val totalLength: Int,
    val rawData: ByteArray,
    val isDataConvertSuccess: Boolean = true
) {


    override fun toString(): String {
        return "( perFix:${ByteUtils.byte2hex(perFix)}, cmd:${
        ByteUtils.byte2hex(cmd)}, dataLength:${dataLength}, data:${
        ByteUtils.byte2hex(data, false)}, endCode:${
        ByteUtils.byte2hex(endCode)}, totalLength:$totalLength)"
    }

    class FailCommand(rawData: ByteArray) :
        DefCommand(0, 0, 0, byteArrayOf(0), 0, 0, rawData, false)
}
