package org.zky.beartooth.demo.lamp

import org.zky.beartooth.adapter.LampCommand
import org.zky.beartooth.msg.BaseMessages
import org.zky.beartooth.utils.ByteUtils
import java.io.OutputStream

class StopLight(isBlockMsg: Boolean = true,
    callback: ((LampCommand?) -> Unit)? = null
) :
    BaseMessages<LampCommand,  Any?>(null, isBlockMsg, callback = callback) {

    override fun writeData(writer: OutputStream) {
        //val byteArray: ByteArray = byteArrayOf(0xAA.toByte(),0x55.toByte(),0x03.toByte(),0x10.toByte(),0xFF.toByte(),0x12.toByte())
        val command = byteArrayOf(0x1, 0x2)
        val m = ByteUtils.byte2hex(command, false)
        writer.write(command)
        writer.flush()
    }

    override fun checkIsRequireData(data: LampCommand): Boolean {
        return data.cmd == LampCmd.CMD_STOP_LIGHT
    }

}