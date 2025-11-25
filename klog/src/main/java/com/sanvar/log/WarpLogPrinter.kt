package com.sanvar.log

class WarpLogPrinter : LogPrinter {

    private var logPrinters = mutableListOf<LogPrinter>()

    override fun printer(priority: Int, message: String) {
        logPrinters.forEach { it.printer(priority, message) }
    }


    fun addLogPrinter(logPrinter: LogPrinter) {
        logPrinters.add(logPrinter)
    }

    fun  removeLogPrinter(logPrinter: LogPrinter) {
        logPrinters.remove(logPrinter)
    }
}