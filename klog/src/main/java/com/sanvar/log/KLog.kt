package com.sanvar.log

import android.util.Log


/**
 *
 */
object KLog {

    private var _priority = Log.DEBUG
    private var _isDebug: Boolean = false
    private var _output: LogPrinter? = null
    private val maxLength = 4000

    /**
     * @param isDebug  if  true , will print log thread name
     * @param minPriority log priority , default is Log.DEBUG
     * @param output log output
     */
    fun setup(isDebug: Boolean, minPriority: Int = Log.DEBUG, output: LogPrinter? = null) {
        _priority = minPriority
        _isDebug = isDebug
        _output = output
    }


    private fun buildMsg(obj: Any?): String {
        if (obj is String) {
            return obj
        }
        if (obj == null) {
            return "null"
        }
        if (!obj.javaClass.isArray) {
            return obj.toString()
        }
        if (obj is Array<*> && obj.isArrayOf<Any>()) {
            return obj.contentDeepToString()
        }
        return obj.toString()
    }

    private fun fileName(): String {
        val stack = Throwable().stackTrace.getOrNull(3) ?: return ""
        return if (_isDebug) {
            "${Thread.currentThread().name}|(${stack.fileName}:${stack.lineNumber}) "
        } else {
            "(${stack.fileName}:${stack.lineNumber}) "
        }
    }


    fun d(msg: () -> Any) {
        if (Log.DEBUG >= _priority) {
            output(Log.DEBUG, msg)
        }
    }


    fun i(msg: () -> Any) {
        if (Log.INFO >= _priority) {
            output(Log.INFO, msg)
        }
    }

    fun w(msg: () -> Any) {
        if (Log.WARN >= _priority) {
            output(Log.WARN, msg)
        }
    }

    fun e(throwable: Throwable? = null, msg: () -> Any) {
        if (Log.ERROR >= _priority) {
            fun finalMsg(): String {
                val thMsg = Log.getStackTraceString(throwable)
                return buildMsg(msg()) + " ,throwable:" + thMsg
            }
            output(Log.ERROR) { finalMsg() }
        }
    }

    fun e(msg: () -> Any) {
        if (Log.ERROR >= _priority) {
            output(Log.ERROR, msg)
        }
    }


    private fun output(priority: Int, msgFun: () -> Any) {
        _output?.let {
            val msg = fileName() + buildMsg(msgFun())

            if (msg.length < maxLength) {
                it.printer(priority, msg)
                return
            }

            var start = 0
            while (start < msg.length) {
                val end = if (start + maxLength < msg.length) {
                    start + maxLength
                } else {
                    msg.length
                }
                val subMessage = msg.substring(start, end)
                it.printer(priority, subMessage)
                start = end
            }
        }
    }
}