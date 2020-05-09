package org.zky.beartooth.adapter

import org.zky.beartooth.utils.ByteUtils

/**
 * Created by nicholas on 2020/4/27.
 */
open class LampCommand(
    val perFix: ByteArray,
    val dataLength: Byte,
    val cmd: Byte,

    val data: ByteArray,
    val checksum: Byte,
    val totalLength: Int,
    val rawData: ByteArray,
    val isDataConvertSuccess: Boolean = true
) {


    override fun toString(): String {
        return "( perFix:${ByteUtils.byte2hex(perFix,false)},dataLength:${dataLength},  cmd:${
        ByteUtils.byte2hex(cmd)}, data:${
        ByteUtils.byte2hex(data, false)}, endCode:${
        ByteUtils.byte2hex(checksum)}, totalLength:$totalLength)"
    }

    class FailCommand(rawData: ByteArray) :
        LampCommand(byteArrayOf(0,0), 0, 0, byteArrayOf(0), 0, 0, rawData, false)
}
