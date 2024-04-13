package com.crazymt.aicodeinterpreter.bean

interface NetCallback<T> {
    fun onSuccess(value: T)
    fun onFail(message: String)
    fun onError(error: String)
}