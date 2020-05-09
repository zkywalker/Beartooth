package org.zky.beartooth.demo.lamp

import org.zky.beartooth.BtTask
import org.zky.beartooth.adapter.*


/**
 * lamp 设备蓝牙任务
 */
class LampTask(address: String) :
    BtTask<LampCommand>(address) {

    // 处理设备发送消息"粘包"的情况
    override fun handleStickyBag(data: ByteArray): MutableList<ByteArray>? {
        val res = ArrayList<ByteArray>()
        checkStickyBag(data, res)
        return res
    }

    fun checkStickyBag(inData: ByteArray, outData: MutableList<ByteArray>) {
        val len = inData[2].toInt()
        //数据长度+头+检查数 = 一条命令的长度
        val cmdLen = len + 3
        if (inData.size > cmdLen) {
            val data = ByteArray(cmdLen)
            val resSize = inData.size - cmdLen
            val remainRawData = ByteArray(resSize)
            System.arraycopy(inData, 0, data, 0, cmdLen)
            System.arraycopy(inData, cmdLen, remainRawData, 0, resSize)
            outData.add(data)
            checkStickyBag(remainRawData, outData)
        } else {
            outData.add(inData)
        }
    }

    override fun registerAdapter(): DataAdapter<LampCommand> {
        return LampCommandAdapter()
    }

}

