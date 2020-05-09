package org.zky.beartooth.demo.lamp

import org.zky.beartooth.adapter.LampCommand
import org.zky.beartooth.msg.BaseMessages
import org.zky.beartooth.utils.ByteUtils
import java.io.OutputStream

class OpenLight(
    msg: OpenLightRequest, isBlockMsg: Boolean = true,
    callback: ((LampCommand?) -> Unit)? = null
) :
    BaseMessages<LampCommand, OpenLightRequest>(msg, isBlockMsg, callback = callback) {

    override fun writeData(writer: OutputStream) {
        val command = byteArrayOf(0x1,0x2)
        val m = ByteUtils.byte2hex(command, false)
        writer.write(command)
        writer.flush()
}

    override fun checkIsRequireData(data: LampCommand): Boolean {
        return data.cmd == LampCmd.CMD_OPEN_LIGHT
    }

}
data class OpenLightRequest(
    var lightNo: Byte,
    var lightValue: Byte
)
