package org.zky.beartooth.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.zky.beartooth.Beartooth
import org.zky.beartooth.State
import org.zky.beartooth.TaskCallback
import org.zky.beartooth.demo.lamp.*
import org.zky.beartooth.adapter.LampCommand
import org.zky.beartooth.debug.L
import org.zky.beartooth.utils.ByteUtils

class MainActivity : AppCompatActivity() {

    private val mac2 = "66:66:66:66:66:66"

    private var task2: LampTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        task2 = buildTask2(mac2)

        test2.setOnClickListener {
            start2()
        }
        test3.setOnClickListener {
            start3()
        }
        test4.setOnClickListener {
            start4()
        }
        test5.setOnClickListener {
            start5()
        }
    }

    private fun buildTask(address: String): LampTask {
//        val task = Beartooth.obtainTask(address) { s ->
//            LampTask(s)
//        } as LampTask
        val task = Beartooth.obtainTask(
            address,
            LampTask::class.java
        )
//        task.sendMessage("hello Beartooth!")
//        task.sendMessage(byteArrayOf(0x1,0x2,0x3,0x4,0x5))

        //注册回调，监听设备主动发送的数据，或者message自己不处理的数据（非阻塞性质的msg）
        //task.callback = this
        task.callback = object : TaskCallback<LampCommand> {
            //处理task状态回调
            override fun onTaskStateChange(o: State, n: State) {
                when (n) {
                    //连上蓝牙
                    is State.Connected -> {

                    }
                    //断开连接的时候
                    is State.Unconnected -> {

                    }
                    //连接失败的情况（还未连上）
                    is State.ConnectedFail -> {

                    }
                }
            }

            //1.处理设备主动发的数据和非阻塞消息数据
            override fun onReceiveData(data: LampCommand) {
                //你可以直接抛到一个方法里处理
                handleCommand(data)
            }
        }
        return task
    }

    private fun buildTask2(address: String): LampTask {
        val task = Beartooth.obtainTask(address) { s ->
            LampTask(s)
        } as LampTask
        //注册回调，监听设备主动发送的数据，或者message自己不处理的数据（非阻塞性质的msg）
        //  task.callback = this
        task.callback = object :
            TaskCallback<LampCommand> {
            override fun onTaskStateChange(o: State, n: State) {
                L.v("MainActivity", "photometer task state = $n")
                Toast.makeText(baseContext, n.toString(), Toast.LENGTH_SHORT).show()
            }

            override fun onReceiveData(data: LampCommand) {
                //你可以直接抛到一个方法里处理
                handleCommand(data)
            }
        }
        return task
    }


    private fun start2() {
        task2?.sendMessages(OpenLight(OpenLightRequest(0x04.toByte(), 0xFF.toByte()), false))
    }

    private fun start3() {
        task2?.sendMessages(StopLight(false))
    }

    private fun start4() {
        task2?.sendMessages(GetValue(500))
    }

    private fun start5() {
        task2?.sendMessages(
            StopGettingValue(false)
        )
    }

    private fun handleCommand(cmd: LampCommand) {
        when (cmd.cmd) {
            LampCmd.CMD_OPEN_LIGHT -> {
            }
            LampCmd.CMD_DEVICE_REPORT -> {
                val data = cmd.data
                if (data.size == 5) {
                    L.v(
                        "MainActivity",
                        "photometer value = ${ByteUtils.getFloat(ByteUtils.subByte(data, 1, 4))}"
                    )
                }
            }
            else -> {
                //do something.
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        task?.close()
//        task2?.close()
//        Beartooth.close(mac)  关闭指定设备
//        Beartooth.closeAll() //关闭所有设备
        //有时候我们要保持蓝牙的连接，我们可以什么都不干
        //但是需要注意置空callback，防止这个页面进入后台或销毁还在接受数据
        task2?.clearCallback()
        //如果我们也不想让现在在执行的消息队列继续执行
        task2?.clearMessage()
        //当然由于是异步的原因，目前正在请求的消息还是会正常执行，这个就跟网络请求是类似的，
        // 要注意这个消息回调的时候这个页面还在不在了，防止崩溃/内存泄露（也可以先不管，概率比较小，用户体量大的话需要注意）

        //下一个页面的话可以正常使用api，sdk内部有判断是否连接，所以不用爬重复连接
        //但是需要注册这个页面的蓝牙回调（同样销毁的时候也要考虑是否置空这个回调）
        task2?.callback = object : TaskCallback<LampCommand> {
            override fun onTaskStateChange(o: State, n: State) {
                //新的页面的回调处理
            }

            override fun onReceiveData(data: LampCommand) {
            }

        }
    }
}
