package com.crazymt.aicodeinterpreter.net

import com.crazymt.aicodeinterpreter.bean.*
import com.google.gson.Gson
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

import java.io.OutputStreamWriter
import java.net.URLEncoder

val SourceOllama = "Ollama(local model)"
val SourceGemini = "Gemini"
val SourceOpenAI = "OpenAI"
val SourceFreeGPT = "FreeGPT"

var sourceType = LocalData.read("sourceType")

var ollamaURL = LocalData.read("ollamaURL")
var modelName = LocalData.read("modelName")

var geminiAPIKey = LocalData.read("geminiAPIKey")
var geminiURL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.0-pro:generateContent"

var openAIURL = LocalData.read("openAIURL")
var openAIModelName = LocalData.read("openAIModelName")
var openAIAPIKey = LocalData.read("openAIAPIKey")

fun requestNetData(file: String?, queryWord: String, callBack: NetCallback<ModelResult>) {
    try {
        val encodedQueryWord = URLEncoder.encode(queryWord.replace(Regex("[*+\\- \r]+"), " "), "UTF-8")
        if (sourceType == SourceFreeGPT) {
            FreeChatGPTAPI.chatWithGPT(file, encodedQueryWord, callBack)
            return
        }

        val (url, requestBody) = when (sourceType) {
            SourceOllama -> Pair(URL(ollamaURL), OllamaRequest(modelName, encodedQueryWord, file).toJson())
            SourceGemini -> Pair(URL("$geminiURL?key=$geminiAPIKey"), GeminiRequest(encodedQueryWord, file).toJson())
            SourceOpenAI -> Pair(URL(openAIURL), OpenAIRequest(openAIModelName, encodedQueryWord, file).toJson())
            else -> throw IllegalArgumentException("Invalid sourceType: $sourceType")
        }

        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 3000
            readTimeout = 30000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            when(sourceType) {
                SourceOpenAI -> {
                    setRequestProperty("Authorization", "Bearer $openAIAPIKey")
                }
            }
            doOutput = true
        }

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(requestBody)
            writer.flush()
        }

        val responseCode = connection.responseCode
        val responseBody = if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.use { it.reader().readText() }
        } else {
            connection.errorStream.use { it?.reader()?.readText() }
        }

        when (sourceType) {
            SourceOllama -> responseBody?.let { handleOllamaResponse(responseCode, it, callBack) }
            SourceGemini -> responseBody?.let { handleGeminiResponse(responseCode, it, callBack) }
            SourceOpenAI -> responseBody?.let { handleOpenAIResponse(responseCode, it, callBack) }
            else -> callBack.onFail("Invalid sourceType: $sourceType")
        }

    } catch (e: IOException) {
        callBack.onFail("Failed to connect: ${e.message}")
    }
}

private fun handleOllamaResponse(responseCode: Int, responseBody: String, callBack: NetCallback<ModelResult>) {
    if (responseCode == HttpURLConnection.HTTP_OK) {
        val result = Gson().fromJson(responseBody, OllamaBean::class.java).toModelResult()
        callBack.onSuccess(result)
    } else {
        callBack.onFail("Error code: $responseCode\nError message:\n$responseBody")
    }
}

private fun handleGeminiResponse(responseCode: Int, responseBody: String, callBack: NetCallback<ModelResult>) {
    if (responseCode == HttpURLConnection.HTTP_OK) {
        val result = Gson().fromJson(responseBody, GeminiBean::class.java).toModelResult()
        callBack.onSuccess(result)
    } else {
        callBack.onFail("Error code: $responseCode\nError message:\n$responseBody")
    }
}

private fun handleOpenAIResponse(responseCode: Int, responseBody: String, callBack: NetCallback<ModelResult>) {
    if (responseCode == HttpURLConnection.HTTP_OK) {
        val result = Gson().fromJson(responseBody, OpenAIBean::class.java).toModelResult()
        callBack.onSuccess(result)
    } else {
        callBack.onFail("Error code: $responseCode\nError message:\n$responseBody")
    }
}

private inline fun <reified T> T.toJson(): String = Gson().toJson(this)



