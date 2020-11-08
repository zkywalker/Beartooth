## Beartooth for classic bluetooth

安卓端传统蓝牙串口的操作封装库。

当你需要连接多种设备、同时连接的时候甚至是需要处理一些响应和粘包问题的时候可以试试这个库。

[![language](https://img.shields.io/badge/language-kotlin-brightgreen)](https://kotlinlang.org/)
[![LICENSE](https://img.shields.io/badge/license-Anti%20996-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE)

### 特性

* 基于Kotlin：使用kotlin开发，调用方便
* 多设备连接：可同时连接多设备蓝牙；多线程处理蓝牙连接，更高效
* 脱离UI：可脱离UI生命周期的蓝牙连接管理
* 快速处理生命周期：封装好的蓝牙连接状态，更清晰的处理连接的生命周期
* 高扩展性：io流读取封装，实现了常见数据类型的解析，支持自定义数据模型解析使业务放更关注业务逻辑
* 常见场景支持：支持消息队列、延迟消息、阻塞式等待消息响应、io流数据粘包拆分等

### 开袋即食

```kotlin
// 根据地址获取该设备的任务
val task = DefaultTask.obtainTask("66:66:66:66:66:66")
// or 标准获取任务的方式
val task = Beartooth.obtainTask("66:66:66:66:66:66", DefaultTask::class.java)
//注册回调（可省略
task.callback = this
// 发送字符串消息
task.sendMessage("Hello Beartooth!")
// 发送byte数组
task.sendMessage(byteArrayOf(0x1,0x2,0x3,0x4,0x5))
// 延迟（100ms）发送消息
task.sendMessage(SimpleMessage("Hello Beartooth！") + 100L)
```

### 详细使用方式

#### 1.创建数据解析的模型

```kotlin
// 构建一个解析蓝牙数据的模型，方便业务方处理业务逻辑。常规的实体类写法，举个例子：
class DefCommand(
    val perFix: Byte,// 命令的前缀
    val cmd: Byte,// 命令标识位
    val dataLength: Int,// 数据长度标识位
    val data: ByteArray,// 实际数据内容
    val endCode: Byte,// 命令的后缀
    val totalLength: Int,// 命令完整的数据长度
    val rawData: ByteArray,// 命令的原始数据
    val isDataConvertSuccess: Boolean = true// 是否转换数据类型成功
) {
		// 可以写个子类处理解析失败的情况
    class FailCommand(rawData: ByteArray) :
        DefCommand(0, 0, 0, byteArrayOf(0), 0, 0, rawData, false)
}
```

#### 2.创建数据解析适配器

```kotlin
class DefCommandAdapter : DataAdapter<DefCommand> {
    // 注册适配器类型
    override fun getType(): Type = DefCommand::class.java
    // 适配数据转换：ByteArray=>目标数据类型
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
            // 记得处理数据转换失败的情况
            L.e("DataAdapter", "convert fail.", e)
            return DefCommand.FailCommand(rawData)
        }
    }

}
```

#### 3.创建对应蓝牙设备的任务类

```kotlin
class LampTask(address: String) : BtTask<DefCommand>(address) {

    // 由于设备返回数据一般不会一下返回，我们需要对多次的数据包检查，是否是完整数据了。
    override fun checkDataEnd(data: ByteArray): Boolean {
        return data[data.size - 1] == LampCmd.SUFFIX
    }
    //  注册数据转换的适配器，如果是框架已实现的数据类型可不实现这个函数
    override fun registerAdapter():DataAdapter<DefCommand>{
        return DefCommandAdapter()
    }
  
    // 处理"粘包"的情况，有的设备可能会一下吐出多条数据，我们需要手动拆分一下
    // 拆分完sdk会一条条的把数据分发出去
  	override fun handleStickyBag(data: ByteArray): MutableList<ByteArray>? {
        val res = ArrayList<ByteArray>()
        checkStickyBag(data, res)
        return res
    }

}
```

#### 4.创建向设备发送的消息的实现类

```kotlin
// 继承超类BtMessage或DefMessage（封装了额外的回调闭包）实现你的消息实体
// 消息的泛型为消息需要接收的数据（Response）和消息发送的数据（Request）
class SetLaserPower(
    msg: Double, isNeedResponse: Boolean = true,// 是否需要响应，需要的话会拦截设备返回的数据自己处理
    callback: ((DefCommand?) -> Unit)? = null// 处理数据的回调
) :
    DefMessage<DefCommand, Double>(msg, isNeedResponse, callback = callback) {

    // 配置等待响应超时的时间
    override var readTimeOut: Long
        get() = 50000L
        set(value) {}
      
     // 处理消息写入的逻辑
    override fun writeData(writer: OutputStream) {
        val m = ByteUtils.byte2hex(
            BlueSendDataUtil.newInstance().setLaserPower(msg), false)
        L.v("BtMessage", "data = $m")
        writer.write(BlueSendDataUtil.newInstance().setLaserPower(msg))
        writer.flush()
    }

    // 检查数据是否是需要该消息需要处理的目标数据，不是的话会重新分发给task的回调
    override fun checkIsRequireData(data: DefCommand): Boolean {
        L.i("BtMessage", "checkIsRequireData: ${data.toString()}")
        return data.cmd == LampCmd.CMD_SET_LASER_POWER
    }

}
```



#### 5.获取指定设备的任务

```kotlin

// 根据地址获取该设备的任务（SDK会缓存这个设备的任务，同一个mac地址不会重复创建任务）
val task = Beartooth.obtainTask(address, LampTask::class.java)
// 也可以通过一个工厂函数来创建对应mac地址的对应任务
val task = Beartooth.obtainTask(address) { s ->
    LampTask(s)
} as LampTask
// 注册回调
task.callback = object : TaskCallback<DefCommand>{
    //处理task状态回调
    override fun onTaskStateChange(o: State, n: State) {
        when(n){
            //连上蓝牙
            is State.Connected ->{

            }
            //断开连接的时候
            is State.Unconnected ->{

            }
            //连接失败的情况（还未连上）
            is State.ConnectedFail ->{

            }
        }
    }
    // 处理设备主动发的数据和非阻塞消息数据
    override fun onReceiveData(data: DefCommand) {
         
    }
}


```

#### 6.发送消息

```kotlin
// 默认会惰性连接设备、读取消息不需要提前连接，需要的话请看连接相关的api
// 发送简单的字符串消息
task.sendMessage(SimpleMessage("hello bluetooth"))
// 发送自定义消息
task.sendMessage(SetLaserPower(0.5))
// 延迟100ms发送消息
task.sendMessageDelay(SetLaserPower(0.5), 100L)
task.sendMessage(SetLaserPower(0.5) + 100L)
// 需要处理设备的响应返回值
task.sendMessage(SetLaserPower(0.5, isNeedResponse = true){
  // 处理回调
})
// 发送消息队列
task.sendMessages (
    SimpleMessage("hello bluetooth"),
    SetLaserPower(0.5),
    SetLaserPower(0.5) + 100L,
    SetLaserPower(0.5, isNeedResponse = true) {
        // 处理回调
    }
)
```

#### 7.其他API

```kotlin
// 获取当前设备连接的情况
task.getCurrentState()
// 主动连接设备
task.connect()
// 主动启动读取数据线程
task.startReadData()
// 断开连接
task.disConnect()
// 关闭设备连接
task.close()
// 关闭指定设备
MultiBluetoothManager.close(mac)  
//关闭所有设备
MultiBluetoothManager.closeAll() 
//有时候我们要保持蓝牙的连接，我们可以什么都不干
//但是需要注意置空callback，防止这个页面进入后台或销毁还在接受数据
task.clearCallback()
//如果我们也不想让现在在执行的消息队列继续执行
task.clearMessage()
// 注册日志代理
L.logLevel = 1
L.setDelegate(object : ILog {
    override fun v(tag: String, msg: String) {
        Log.v(tag, msg)
    }

    override fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    override fun e(tag: String, msg: String, e: Throwable) {
        Log.e(tag, msg, e)
    }

})
```


#### TODO LIST

* 搜索附近设备相关API
* accept模式API
* 线程休眠阻塞时间性能优化
* 并发情况支持
* 协程支持




