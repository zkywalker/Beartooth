package org.zky.beartooth

/**
 * Created by zhangkun on 2020/4/7 Tuesday.
 */
interface TaskCallback<DataType> : BtCallback<DataType> {

    fun onTaskStateChange(o: State, n: State)

}

interface BtCallback<DataType> {

    fun onReceiveData(data: DataType)

}