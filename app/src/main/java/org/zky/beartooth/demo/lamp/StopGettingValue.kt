package org.zky.beartooth.demo.lamp

import org.zky.beartooth.adapter.LampCommand
import org.zky.beartooth.debug.L
import org.zky.beartooth.msg.BaseMessages
import org.zky.beartooth.utils.ByteUtils
import java.io.OutputStream

class StopGettingValue(isBlockMsg: Boolean = true,
                       callback: ((LampCommand?) -> Unit)? = null
) :
    BaseMessages<LampCommand,  Any?>(null, isBlockMsg, callback = callback) {

    override fun writeData(writer: OutputStream) {
        val command = byteArrayOf(0x1)
        val m = ByteUtils.byte2hex(command, false)
        writer.write(command)
        writer.flush()
    }

    override fun checkIsRequireData(data: LampCommand): Boolean {
        return data.cmd == LampCmd.CMD_STOP_GETTING_VALUE
    }

}