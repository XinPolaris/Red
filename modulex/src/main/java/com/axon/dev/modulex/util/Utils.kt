package com.axon.dev.modulex.util

internal object Utils {
    fun getDefaultValueForPrimitiveType(type: Class<*>): Any? {
        if (type == Boolean::class.java || type == Boolean::class.javaPrimitiveType) {
            return false
        }
        if (type == Byte::class.java || type == Byte::class.javaPrimitiveType) {
            return 0.toByte()
        }
        if (type == Char::class.java || type == Char::class.javaPrimitiveType) {
            return Character.valueOf(0.toChar())
        }
        if (type == Short::class.java || type == Short::class.javaPrimitiveType) {
            return 0.toShort()
        }
        if (type == Int::class.java || type == Int::class.javaPrimitiveType) {
            return 0
        }
        if (type == Float::class.java || type == Float::class.javaPrimitiveType) {
            return 0
        }
        if (type == Double::class.java || type == Double::class.javaPrimitiveType) {
            return 0
        }
        if (type == Long::class.java || type == Long::class.javaPrimitiveType) {
            return 0
        }
        return null
    }
}