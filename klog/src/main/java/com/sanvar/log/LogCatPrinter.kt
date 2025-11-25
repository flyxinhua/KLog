package com.sanvar.log

import android.util.Log

class LogCatPrinter(val tag: String) : LogPrinter {

    override fun printer(priority: Int, message: String) {
        Log.println(priority, tag, message)
    }
}