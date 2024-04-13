package com.crazymt.aicodeinterpreter.net

interface NetCallback<T> {
    fun onSuccess(value: T)
    fun onFail(message: String)
    fun onError(error: String)
}