package com.crazymt.aicodeinterpreter.net

import com.crazymt.aicodeinterpreter.bean.ModelResult
import com.crazymt.aicodeinterpreter.bean.OpenAIStreamBean
import com.crazymt.aicodeinterpreter.bean.OpenAIToken
import com.google.gson.Gson
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object FreeChatGPTAPI {
    private const val baseUrl = "https://chat.openai.com"
    private const val apiUrl = "$baseUrl/backend-api/conversation"
//    private const val refreshInterval: Long = 60000 // Interval to refresh token in ms
//    private const val errorWait: Long = 120000 // Wait time in ms after an error

    private var token: String = ""
    private var oaiDeviceId: String? = null

    @Throws(Exception::class)
    fun chatWithGPT(file: String?, encodedQueryWord: String, callBack: NetCallback<ModelResult>) {
        val newDeviceId = UUID.randomUUID().toString()
        val url = URL("$baseUrl/backend-anon/sentinel/chat-requirements")
        val connection = (url.openConnection() as? HttpURLConnection)?.apply {
            requestMethod = "POST"
            connectTimeout = 30000
            readTimeout = 30000
            setRequestProperties(this, newDeviceId)
            doOutput = true
        }

        if (connection?.responseCode == 200) {
            val content = StreamUtils.getStringFromStream(connection.inputStream)
            if (content.isNotBlank()) {
                val result = Gson().fromJson(content, OpenAIToken::class.java)
                token = result.token
                oaiDeviceId = newDeviceId
                handleChatCompletion(file, encodedQueryWord, callBack, "")
            } else {
                println("MTMTMT System: fail content:" + connection.responseMessage)
                callBack.onFail("System: fail content:" + connection.responseMessage)
            }
        } else {
            println("MTMTMT System: fail content:" + connection?.responseMessage)
            callBack.onFail("System: fail content:" + connection?.responseMessage)
        }
    }


    private fun setRequestProperties(connection: HttpURLConnection, newDeviceId: String) {
        with(connection) {
            setRequestProperty("oai-device-id", newDeviceId)
            setRequestProperty("accept", "*/*")
            setRequestProperty("accept-language", "zh-CN,zh;q=0.9")
            setRequestProperty("content-type", "application/json")
            setRequestProperty("oai-device-id", newDeviceId)
            setRequestProperty("oai-language", "en-US")
            setRequestProperty("origin", baseUrl)
            setRequestProperty("referer", baseUrl)
            setRequestProperty(
                "user-agent",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1 Edg/123.0.0.0"
            )
        }
    }

    private fun handleChatCompletion(file: String?, encodedQueryWord: String, callBack: NetCallback<ModelResult>, source: String): String? {
        var result = ""
        try {
            val url = URL(apiUrl)
            val con = (url.openConnection() as? HttpURLConnection)?.apply {
                requestMethod = "POST"
                setRequestProperty("accept", "text/event-stream")
                setRequestProperty("accept-language", "zh-CN,zh;q=0.9")
                setRequestProperty("content-type", "application/json")
                setRequestProperty("oai-device-id", oaiDeviceId)
                setRequestProperty("oai-language", "en-US")
                setRequestProperty("openai-sentinel-chat-requirements-token", token)
                setRequestProperty("origin", "https://chat.openai.com")
                setRequestProperty("referer", "https://chat.openai.com/")
                setRequestProperty(
                    "user-agent",
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1 Edg/123.0.0.0"
                )
                doOutput = true
            }

            val jsonInputString = """
                    {
                        "action": "next",
                        "messages": [
                            {
                                "id": "${UUID.randomUUID()}",
                                "author": {
                                    "role": "user"
                                },
                                "content": {
                                    "content_type": "text",
                                    "parts": [
                                        "You are a senior software engineer. This piece of code is a part of the ${file ?: "unknown"} file. I need you to explain its purpose and provide a code example. Please keep it concise. Code: $encodedQueryWord."
                                    ]
                                },
                                "metadata": {}
                            }
                        ],
                        "parent_message_id": "${UUID.randomUUID()}",
                        "model": "text-davinci-002-render-sha",
                        "timezone_offset_min": -480,
                        "history_and_training_disabled": false,
                        "conversation_mode": {
                            "kind": "primary_assistant"
                        },
                        "force_paragen": false,
                        "force_paragen_model_slug": "",
                        "force_nulligen": false,
                        "force_rate_limit": false,
                        "websocket_request_id": "${UUID.randomUUID()}"
                    }
                    
                """.trimIndent()

            DataOutputStream(con?.outputStream).use { out ->
                out.write(jsonInputString.toByteArray())
            }

            val responseCode = con?.responseCode
            println("MTMTMT Response Code : $responseCode")

            con?.inputStream?.let { InputStreamReader(it) }?.let {
                BufferedReader(it).useLines { lines ->
                    lines.forEach { line ->
                        try {
//                            println("MTMTMT: $line")
                            val resultData = Gson().fromJson(line.removePrefix("data: "), OpenAIStreamBean::class.java)
                            if (resultData.message.metadata.is_complete) {
                                result = resultData.message.content.parts[0]
                            }
                        } catch (e: Exception) {

                        }
                    }
                }
            }

            if (result.isNotEmpty()) {
                callBack.onSuccess(ModelResult(SourceFreeGPT, "en", "zh", source, result, ""))
            } else {
                callBack.onFail("System: fail content:" + con?.responseCode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callBack.onFail("System: Exception content:" + e.message)
        }
        return result
    }
}
