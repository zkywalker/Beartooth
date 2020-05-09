package org.zky.beartooth.demo.lamp

import org.zky.beartooth.adapter.LampCommand
import org.zky.beartooth.msg.BaseMessages
import java.io.OutputStream

class GetValue(
    msg: Int, isNeedResponse: Boolean = true,
    callback: ((LampCommand?) -> Unit)? = null
) :
    BaseMessages<LampCommand, Int>(msg, isNeedResponse, callback = callback) {

    override fun writeData(writer: OutputStream) {
        val command = byteArrayOf(0x1,0x2)
        writer.write(command)
        writer.flush()
    }

    override fun checkIsRequireData(data: LampCommand): Boolean {
        return data.cmd == LampCmd.CMD_GET_VALUE
    }

}
