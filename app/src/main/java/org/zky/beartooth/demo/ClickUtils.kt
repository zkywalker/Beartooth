package org.zky.beartooth.demo

import android.view.View

/**
 * Created by zhangkun on 2020/4/27 Monday.
 */

fun View.setNoDoubleClick( delay: Int = 1000,  click: () -> Unit){

    val onClick = object :View.OnClickListener{
        var time = 0L;

        override fun onClick(v: View?) {
            val current = System.currentTimeMillis()
            if (current > time + delay) {
                time = current
                click()
            }
        }
    }
    setOnClickListener(onClick)

}