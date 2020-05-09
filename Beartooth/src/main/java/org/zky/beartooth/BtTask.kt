package org.zky.beartooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Message
import android.util.Log
import org.zky.beartooth.adapter.DataAdapter
import org.zky.beartooth.debug.L
import org.zky.beartooth.msg.BtMessage
import org.zky.beartooth.msg.MultiMessage
import org.zky.beartooth.utils.getType
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Thread.sleep
import java.lang.reflect.Type
import java.util.concurrent.LinkedBlockingQueue
import kotlin.properties.Delegates


/**
 * Created by zhangkun on 2020/4/7 Tuesday.
 */

abstract class BtTask<DataType : Any>(val mDeviceAddress: String) : Closeable {

    internal var mCurrState: State by Delegates.observable(State.Unconnected() as State)
    { p, o, n ->
        if (o.javaClass != n.javaClass) {
            Beartooth.post {
                callback?.onTaskStateChange(o, n)
            }
            var msg = ""
            if (n is State.Unconnected) {
                msg = n.msg
            }
            L.i(TAG, "onTaskStateChange: state ->${n.javaClass.simpleName},$msg")
        }
    }

    fun getCurrentState() = mCurrState

    @SuppressLint("HandlerLeak")
    val timeOutHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (mCurrentBlockingMessage != null) {
                L.i("timeout", "current block message time out，time = ${msg.what}")
                val temp = mCurrentBlockingMessage
                mCurrentBlockingMessage = null
                temp?.handleTimeout()
//                if (temp?.handleTimeout() == true) {
//                    temp.retryCount =  temp.retryCount + 1
//                }
                handleWrite()
            }
            //todo 超时需要回调吗
        }
    }

    var dataType: Type? = null
        get() {
            if (field == null) {
                field = getType(javaClass.genericSuperclass!!)
            }
            return field
        }

    var callback: TaskCallback<in DataType>? = null

    private var mSocket: BluetoothSocket? = null

    private var mInputStream: InputStream? = null

    private var mOutputStream: OutputStream? = null

    private val mMessageQueue: LinkedBlockingQueue<BtMessage<*, *>> =
        LinkedBlockingQueue<BtMessage<*, *>>()

    @Volatile
    private var mCurrentBlockingMessage: BtMessage<*, *>? = null

    @Volatile
    internal var mReadable = false

    @Volatile
    internal var mWritable = false


    inner class ConnectRunnable : Runnable {
        override fun run() {
            if (mCurrState !is State.Connected && mCurrState !is State.Connecting) {
                connect()
            }
        }

        fun connect() {
            val remoteDevice =
                Beartooth.mBluetoothAdapter.getRemoteDevice(mDeviceAddress)
            Beartooth.mBluetoothAdapter.cancelDiscovery()
            mCurrState = State.Connecting()
            // todo issue：特殊设备可能有问题
            // BluetoothSocket socket =  (BluetoothSocket) remoteDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(remoteDevice,1);

            try {
                mSocket =
                    remoteDevice.createInsecureRfcommSocketToServiceRecord(Beartooth.mUUID)
                mSocket?.apply {
                    L.i("connect", "try to connect mSocket")
                    connect()
                    mInputStream = inputStream
                    mOutputStream = outputStream
                    L.i("connect", "connect to mSocket,$mInputStream,$mOutputStream")
                    mCurrState = State.Connected()
                }
            } catch (e: Exception) {
                try {
                    mInputStream?.close()
                    mOutputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                clearRes(e.message)
                mCurrState = State.ConnectedFail(e.message ?: "")
                L.v(TAG, e.message ?: "null")
            }
        }

    }

    /**
     * 异常导致连接失败的时候
     */
    private fun clearRes(message: String?) {
        mReadable = false
        mWritable = false
        mInputStream = null
        mOutputStream = null
        mSocket = null
    }

    inner class ReadDataRunnable : Runnable {

        override fun run() {
            if (!mReadable) {
                startReadData()
            }
        }

        fun startReadData() {
            mReadable = true
            // 10ms 检查一次是否连接好了
            if (mCurrState !is State.Connected){
                L.v(TAG, "ReadData:waiting for connecting...")
            }
            while (mCurrState !is State.Connected) {
                sleep(100)
                if (!mReadable) {
                    L.v(TAG, "ReadData:stop waiting.")
                    return
                }
            }
            val tempIS = mInputStream ?: return
            var data = ByteArray(0)
            var bufferLength = 0
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (mReadable) {
                L.v(TAG, "ReadData:try to read data...")
                try {
                    while (tempIS.available() == 0) {
//                        L.v(TAG, "ReadData:no data,waiting...")
                        sleep(1000)
                    }
                    bufferLength = tempIS.read(buffer)
                    //读到数据进行复制
                    if (bufferLength != 0) {
                        val temp = data
                        data = ByteArray(data.size + bufferLength)
                        System.arraycopy(temp, 0, data, 0, temp.size)
                        System.arraycopy(buffer, 0, data, temp.size, bufferLength)
                    }
                    //当一个包结束了，确定是否是结尾，把数据返回给用户
                    if (bufferLength != DEFAULT_BUFFER_SIZE && checkDataEnd(data)) {
                        Log.v("readData", "data len = ${data.size}")
                        //先检查下是否有粘包的情况
                        val datas = handleStickyBag(data)
                        if (datas != null && datas.size > 1) {
                            L.i(
                                "sticky bag",
                                "--------------------find stick bag! start-------------------"
                            )
                            datas.forEach {
                                dispatchData(it)
                            }
                            L.i(
                                "sticky bag",
                                "--------------------find stick bag!   end-------------------"
                            )
                        } else {
                            dispatchData(data)
                        }
                        data = byteArrayOf()
                    }
                } catch (e: Exception) {
                    clearRes(e.message)
                    mCurrState = State.Unconnected(e.message ?: "")
                    L.e(TAG, "", e)
                    break
                }
            }

        }

    }

    /**
     * 拆分下粘包的情况
     */
    open fun handleStickyBag(data: ByteArray): MutableList<ByteArray>? = null

    private fun dispatchData(data: ByteArray) {
        //先让当前阻塞式的消息自己处理
        if (mCurrentBlockingMessage != null &&
            mCurrentBlockingMessage?.handleData(data) == true
        ) {
            L.i(TAG, "ReadData: read block message")
            //todo 这个地方有并发风险
            mCurrentBlockingMessage = null
            //清除超时检查
            timeOutHandler.removeCallbacksAndMessages(null)
            handleWrite()
        } else {
            onReceiveData(Beartooth.convert<DataType>(dataType, data))
            L.i(TAG, "ReadData: data = $data")
        }
    }

    fun obtainWriter(): Runnable = WriteDataRunnable()

    fun obtainReader(): Runnable = ReadDataRunnable()

    fun obtainConnector(): Runnable = ConnectRunnable()

    inner class WriteDataRunnable : Runnable {
        override fun run() {
            if (!mWritable) {
                writeData()
            }
        }

        fun writeData() {
            mWritable = true
            if (mCurrState !is State.Connected){
                L.v(TAG, "writeData:waiting for connecting...")
            }
            while (mCurrState !is State.Connected) {
                sleep(100)
                //检查是否断链了
                if (!mWritable) {
                    L.v(TAG, "writeData:stop waiting.")
                    return
                }
            }
            val tempOs = mOutputStream ?: return
            L.i(TAG, "writeData:total message in queue = ${mMessageQueue.size}")
            try {
//                val writer = BufferedWriter(OutputStreamWriter(tempOs))
                while (mWritable) {
                    val msg = mMessageQueue.poll() ?: break
                    try {
                        if (msg.delayMs > 0) {
                            L.i(TAG, "writeData:delay send msg,delay = ${msg.delayMs}")
                            sleep(msg.delayMs)
                        }
                        L.v(TAG, "writeData:start write data...")
                        msg.writeData(tempOs)
                        L.i(
                            TAG,
                            "writeData: write done, msg type = ${msg.javaClass.simpleName}\n msg data = ${msg.msg}"
                        )
                        msg.sendTS = System.currentTimeMillis()
                    } catch (e: Exception) {
                        //todo 异常处理
                        try {
                            tempOs.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        clearRes(e.message)
                        mCurrState = State.Unconnected(e.message ?: "")
                        L.e(TAG, "", e)
                        break
                    }
                    if (msg.isNeedResponse) {
                        mWritable = false
                        mCurrentBlockingMessage = msg
                        timeOutHandler.sendEmptyMessageDelayed(
                            msg.readTimeOut.toInt(),
                            msg.readTimeOut
                        )
                        break
                    }
                }
            } catch (ex: Exception) {
                clearRes(ex.message)
                mCurrState = State.Unconnected(ex.message ?: "")
                L.e(TAG, "", ex)
            }
            mWritable = false
        }
    }

    private fun handleWrite() {
        if (mMessageQueue.isNotEmpty()) {
            Beartooth.write(this)
        }
    }

    fun connect() {
        if (mCurrState !is State.Connected && mCurrState !is State.Connecting) {
            L.i(TAG, "start connect thread->")
            Beartooth.connect(this)
        }
    }

    fun startReadData() {
        if (!mReadable) {
            L.i(TAG, "start read thread->")
            Beartooth.read(this)
        }
    }

    fun startWriteData() {
        if (!mWritable) {
            L.i(TAG, "start write thread->")
            Beartooth.write(this)
        }
    }

    fun sendMessages(vararg msg: BtMessage<*, *>, needReadData: Boolean = true) {
        mMessageQueue.addAll(msg)
        //先检查下连接、读数据线程是否就绪
        connect()
        if (needReadData) {
            startReadData()
        }
        if (mCurrentBlockingMessage == null) {
            startWriteData()
        } else {
            L.i("sendMsg", "has block message, just add queue")
        }
    }

    fun sendMessage(msg: BtMessage<*, *>, needReadData: Boolean = true) {
        mMessageQueue.add(msg)
        //先检查下连接、读数据线程是否就绪
        connect()
        if (needReadData) {
            startReadData()
        }
        if (mCurrentBlockingMessage == null) {
            startWriteData()
        } else {
            L.i("sendMsg", "has block message, just add queue")
        }
    }

    fun sendMessage(msg: String, needReadData: Boolean = true) {
        sendMessage(MultiMessage<ByteArray>(msg), needReadData)
    }


    fun sendMessage(msg: ByteArray, needReadData: Boolean = true) {
        sendMessage(MultiMessage<ByteArray>(msg), needReadData)
    }

    fun sendMessageDelay(msg: BtMessage<*, *>, ms: Long) {
        msg.delayMs = ms
        sendMessage(msg)
    }

    open fun registerAdapter(): DataAdapter<DataType>? = null

    /**
     * 检查数据是否到尾部了
     * @param data 当前积累的数据（默认是一个包的数据），需要可以重新指定一条命令的完整数据长度
     * @return 当前是否结束了
     */
    open fun checkDataEnd(data: ByteArray) = true

    open fun onReceiveData(data: DataType) {
        L.data(this, "onReceiveData", data.toString())
        Beartooth.post {
            callback?.onReceiveData(data)
        }
    }

    fun clearCallback(){
        callback = null
    }

    fun clearMessage(){
        mCurrentBlockingMessage = null
        timeOutHandler.removeCallbacksAndMessages(null)
        mMessageQueue.clear()
    }

    fun disConnect() {
        mReadable = false
        mWritable = false
        clearMessage()
        try {
            mSocket?.close()
            mInputStream?.close()
            mOutputStream?.close()
        } catch (e: java.lang.Exception) {
            L.e("closeTask", "", e)
        } finally {
            mSocket = null
            mInputStream = null
            mOutputStream = null
        }
        //todo 回调应该先抛还是后抛？
        mCurrState = State.Unconnected(isManual = true)
    }

    override fun close() {
        disConnect()
        callback = null
    }

    companion object {

        const val TAG = "BtTask"

        const val DEFAULT_BUFFER_SIZE = 1024

        const val WRITE_TIME_OUT = 5000

        const val READ_TIME_OUT = 10000
    }

}