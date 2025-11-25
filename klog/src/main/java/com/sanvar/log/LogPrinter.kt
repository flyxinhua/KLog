package com.sanvar.log

interface LogPrinter {
    fun printer(priority: Int, message: String)
}