package org.zky.beartooth.adapter

import android.util.Base64
import org.zky.beartooth.debug.L
import org.zky.beartooth.utils.ByteUtils
import java.lang.Exception
import java.lang.reflect.Type

/**
 * Created by zhangkun on 2020/4/10 Friday.
 */

interface DataAdapter<T> {
    fun getType(): Type

    fun convert(rawData: ByteArray): T
}


class StringAdapter : DataAdapter<String> {

    override fun getType(): Type = String::class.java

    override fun convert(rawData: ByteArray): String = String(rawData)

}

class ByteArrayAdapter : DataAdapter<ByteArray> {

    override fun getType(): Type = String::class.java

    override fun convert(rawData: ByteArray): ByteArray = rawData

}

class HexAdapter : DataAdapter<Hex> {

    override fun getType(): Type = Hex::class.java

    override fun convert(rawData: ByteArray): Hex = Hex(ByteUtils.byte2hex(rawData, false))

}

class CharArrayAdapter : DataAdapter<CharArray> {

    override fun getType(): Type = CharArray::class.java

    override fun convert(rawData: ByteArray): CharArray = ByteUtils.getChars(rawData)

}


class Base64Adapter : DataAdapter<DecodedBase64> {

    override fun getType(): Type = DecodedBase64::class.java

    override fun convert(rawData: ByteArray): DecodedBase64 = DecodedBase64(
        String(Base64.decode(rawData, Base64.DEFAULT))
    )

}

class DefCommandAdapter : DataAdapter<DefCommand> {

    override fun getType(): Type = DefCommand::class.java

    override fun convert(rawData: ByteArray): DefCommand {

        try {
            val dataLength = byteArrayOf(0x00, 0x00, rawData[2], rawData[3])
            val data = ByteUtils.subByte(rawData, 4, rawData.size - 5)
            return DefCommand(
                rawData[0],
                rawData[1],
                ByteUtils.byteArrayToInt(dataLength),
                data,
                rawData[rawData.size - 1],
                rawData.size,
                rawData
            )
        } catch (e: Exception) {
            L.e("DataAdapter", "convert fail.", e)
            return DefCommand.FailCommand(rawData)
        }
    }

}

class LampCommandAdapter : DataAdapter<LampCommand> {

    override fun getType(): Type = LampCommand::class.java

    override fun convert(rawData: ByteArray): LampCommand {

        try {
            val data = ByteUtils.subByte(rawData, 4, rawData.size - 5)
            return LampCommand(byteArrayOf(rawData[0],
                rawData[1]),rawData[2],rawData[3],
                data,
                rawData[rawData.size - 1],
                rawData.size,
                rawData
            )
        } catch (e: Exception) {
            L.e("DataAdapter", "convert fail.", e)
            return LampCommand.FailCommand(rawData)
        }
    }

}