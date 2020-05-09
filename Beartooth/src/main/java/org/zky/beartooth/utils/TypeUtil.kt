package org.zky.beartooth.utils

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

/**
 * Created by zhangkun on 2020/4/16 Thursday.
 */
public fun getType(type: Type): Type {
    when (type) {
        is ParameterizedType -> {
            return getGenericType(type)
        }
        is TypeVariable<*> -> {
            return getType(type.bounds[0])
        }
    }
    return type
}

public fun getGenericType(type: ParameterizedType): Type {
    if (type.actualTypeArguments.isEmpty()) return type
    val actualType = type.actualTypeArguments[0]
    when (actualType) {
        is ParameterizedType -> {
            return actualType.rawType
        }
        is GenericArrayType -> {
            return actualType.genericComponentType
        }
        is TypeVariable<*> -> {
            return getType(actualType.bounds[0])
        }
    }
    return actualType
}
