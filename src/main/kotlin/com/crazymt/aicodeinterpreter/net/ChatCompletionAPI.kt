package com.crazymt.aicodeinterpreter.net

import com.crazymt.aicodeinterpreter.bean.OpenAIStreamBean
import com.crazymt.aicodeinterpreter.bean.OpenAIToken
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object ChatCompletionAPI {
    private const val baseUrl = "https://chat.openai.com"
    private const val apiUrl = baseUrl + "/backend-api/conversation"
    private const val refreshInterval: Long = 60000 // Interval to refresh token in ms
    private const val errorWait: Long = 120000 // Wait time in ms after an error

    // Initialize global variables to store the session token and device ID
    private var token: String = ""
    private var oaiDeviceId: String? = null

    @get:Throws(Exception::class)
    val newSessionId: Unit
        // Function to get a new session ID and token from the OpenAI API
        get() {
            val newDeviceId = UUID.randomUUID().toString()
            val url = URL(baseUrl + "/backend-anon/sentinel/chat-requirements")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.setRequestProperty("oai-device-id", newDeviceId)
            connection.setRequestProperty("accept", "*/*")
            connection.setRequestProperty("accept-language", "zh-CN,zh;q=0.9")
            connection.setRequestProperty("content-type", "application/json")
            connection.setRequestProperty("oai-device-id", newDeviceId)
            connection.setRequestProperty("oai-language", "en-US")
            connection.setRequestProperty("origin", baseUrl)
            connection.setRequestProperty("referer", baseUrl)
//            connection.setRequestProperty("sec-fetch-dest", "empty")
//            connection.setRequestProperty("sec-fetch-mode", "cors")
//            connection.setRequestProperty("sec-fetch-site", "same-origin")
            connection.setRequestProperty(
                "user-agent",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1 Edg/123.0.0.0"
            )
            connection.doOutput = true

            val outputStream = OutputStreamWriter(connection.outputStream)
            outputStream.flush()

            if (connection.responseCode == 200) {
                val ins = connection.inputStream

                val content = StreamUtils.getStringFromStream(ins)

                println("MTMTMT System: Successfully content:" + content)
                if (content.isNotBlank()) {
                    val result = Gson().fromJson(content, OpenAIToken::class.java)
                    token = result.token
                    oaiDeviceId = newDeviceId

                    handleChatCompletion("你好")
                } else {
                    println("MTMTMT System: fail content:" + connection.responseMessage)
                }
            } else {
                println("MTMTMT System: fail content:" + connection.responseMessage)
            }
        }

    fun handleChatCompletion(prompt: String?): String? {
        var result = ""
        try {
            val url = URL(apiUrl)
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.setRequestProperty("accept", "text/event-stream")
            con.setRequestProperty("accept-language", "zh-CN,zh;q=0.9")
            con.setRequestProperty("content-type", "application/json")
            con.setRequestProperty("oai-device-id", oaiDeviceId)
            con.setRequestProperty("oai-language", "en-US")
            con.setRequestProperty(
                "openai-sentinel-chat-requirements-token",
                token
            )
            con.setRequestProperty("origin", "https://chat.openai.com")
            con.setRequestProperty("referer", "https://chat.openai.com/")
//            con.setRequestProperty("sec-fetch-dest", "empty")
//            con.setRequestProperty("sec-fetch-mode", "cors")
//            con.setRequestProperty("sec-fetch-site", "same-origin")
            con.setRequestProperty(
                "user-agent",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1 Edg/123.0.0.0"
            )
            con.doOutput = true

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
                                    "$prompt"
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

            DataOutputStream(con.outputStream).use { out ->
                out.write(jsonInputString.toByteArray())
            }
            val responseCode = con.responseCode
            println("MTMTMT Response Code : $responseCode")

            val `in` = BufferedReader(InputStreamReader(con.inputStream))
            var inputLine: String?
            val response = StringBuilder()
            while ((`in`.readLine().also { inputLine = it }) != null) {
                response.append(inputLine)

                try {
                    val resultData = Gson().fromJson(inputLine?.removePrefix("data: "), OpenAIStreamBean::class.java)
                    if (resultData.message.metadata.is_complete) {
                        result = resultData.message.content.parts[0]
                    }
                } catch (e: Exception) {

                }
            }
            `in`.close()

            // Print result
            println("MTMTMT:" + result)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return result
    }
}
