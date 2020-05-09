package org.zky.beartooth

/**
 * Created by zhangkun on 2020/4/7 Tuesday.
 */
sealed class State(val msg: String) {

    val time: Long = System.currentTimeMillis()

    class Discovering(msg: String = "") : State(msg)

    class Connecting(msg: String = "") : State(msg)

    class Connected(msg: String = "") : State(msg)

    class ConnectedFail(msg: String = "") : State(msg)

    class Unconnected(msg: String = "", val isManual: Boolean = false) : State(msg)

}