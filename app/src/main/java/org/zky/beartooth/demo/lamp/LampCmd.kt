package org.zky.beartooth.demo.lamp

object  LampCmd {

    val PREFIX :ByteArray = byteArrayOf(0xAA.toByte() ,0x55.toByte())

    const val CMD_DEVICE_SELF_TESTS = 0x01.toByte()

    const val CMD_STOP_LIGHT = 0x02.toByte()

    const val CMD_OPEN_LIGHT  = 0x03.toByte()

    const val CMD_GET_VALUE = 0x04.toByte()

    const val CMD_STOP_GETTING_VALUE  = 0x05.toByte()

    const val CMD_DEVICE_REPORT = 0x06.toByte()

}