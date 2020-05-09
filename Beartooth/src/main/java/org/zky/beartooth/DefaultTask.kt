package org.zky.beartooth

class DefaultTask(mDeviceAddress: String) : BtTask<ByteArray>(mDeviceAddress) {

    companion object {
        fun obtainTask(address: String): DefaultTask =
            Beartooth.obtainTask(address, DefaultTask::class.java)
    }

}