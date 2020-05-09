package org.zky.beartooth

import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.os.Looper
import org.zky.beartooth.adapter.*
import java.lang.reflect.Type
import java.util.concurrent.Executors
import kotlin.collections.HashMap

/**
 * todo 没有考虑并发的问题
 * Created by zhangkun on 2020/4/7 Tuesday.
 */
object Beartooth {

    private val mTasks: HashMap<String, BtTask<*>> = HashMap()

    private val mAdapterFactories: HashMap<Type?, DataAdapter<*>> = HashMap()

    private val mExecutor = Executors.newCachedThreadPool()

    val handler = Handler(Looper.getMainLooper())

    var mUUID = "00001101-0000-1000-8000-00805F9B34FB".toUUID()

    val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    init {
        StringAdapter().apply {
            mAdapterFactories[getType()] = this
        }
        ByteArrayAdapter().apply {
            mAdapterFactories[getType()] = this
        }
        HexAdapter().apply {
            mAdapterFactories[getType()] = this
        }
        CharArrayAdapter().apply {
            mAdapterFactories[getType()] = this
        }
        Base64Adapter().apply {
            mAdapterFactories[getType()] = this
        }

    }

    fun connect(task: BtTask<*>) {
        mExecutor.submit(task.obtainConnector())
    }

    fun read(task: BtTask<*>) {
        mExecutor.submit(task.obtainReader())
    }

    fun write(task: BtTask<*>) {
        mExecutor.submit(task.obtainWriter())
    }

    fun <T> convert(type: Type?, rowData: ByteArray): T {
        val dataAdapter = mAdapterFactories[type] ?: throw NullPointerException(
            "no find adapter of ${type?.typeName}"
        )
        return dataAdapter.convert(rowData) as T
    }

    fun <T : BtTask<*>> obtainTask(address: String, taskClass: Class<T>): T {
        val btTask = mTasks[address]
        val task: T = if (btTask == null) {
            val task = buildTask(address, taskClass)
            registerTask(task)
            task
        } else {
            btTask as T
        }
        return task
    }

    private fun <T : BtTask<*>> buildTask(address: String, taskClass: Class<T>): T {
        val constructor = taskClass.getConstructor(String::class.java)
        return constructor.newInstance(address)
    }

    fun registerTask(task: BtTask<*>) {
        mTasks[task.mDeviceAddress] = task
        val dataAdapter = mAdapterFactories[task.dataType]
        if (dataAdapter == null) {
            val registerAdapter = task.registerAdapter()
            if (registerAdapter != null) {
                mAdapterFactories[registerAdapter.getType()] = registerAdapter
            }
        }
    }

    fun obtainTask(
        address: String,
        mTaskFactory: (String) -> BtTask<*>
    ): BtTask<*> {
        val btTask = mTasks[address]
        val task = if (btTask == null) {
            val task = mTaskFactory(address)
            registerTask(task)
            task
        } else {
            btTask
        }
        return task
    }

    fun closeAll() {
        mTasks.entries.forEach {
            it.value.close()
        }
    }

    fun close(address: String) {
        mTasks[address]?.close()
    }

    fun post(runnable: () -> Unit) {
        handler.post(runnable)
    }

}