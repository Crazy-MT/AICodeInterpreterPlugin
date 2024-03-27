package com.crazymt.aicodeinterpreter.net

import com.crazymt.aicodeinterpreter.bean.*
import com.google.gson.Gson
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

import java.io.OutputStreamWriter
import java.net.URLEncoder

val SourceOllama = "Ollama(本地模型)"
val SourceGemini = "Gemini"
val SourceOpenAI = "OpenAI"

var sourceType = LocalData.read("sourceType")

var ollamaURL = LocalData.read("ollamaURL")
var modelName = LocalData.read("modelName")
var geminiAPIKey = LocalData.read("geminiAPIKey")

var openAIURL = LocalData.read("openAIURL")
var openAIModelName = LocalData.read("openAIModelName")
var openAIAPIKey = LocalData.read("openAIAPIKey")

fun requestNetData(file: String?, queryWord: String, callBack: NetCallback<ModelResult>) {
    try {
        /*LocalData.read(queryWord)?.let {
            try {
                val bean = Gson().fromJson<TranslateResult>(it, TranslateResult::class.java)
                callBack.onSuccess(bean)
            } catch (e: JsonSyntaxException) {
                callBack.onFail(" 返回解析失败，github issue to me：\n$it")
            }
            return
        }*/
        val queryStr = URLEncoder.encode(queryWord.replace(Regex("[*+\\- \r]+"), " "), "UTF-8")

        when (sourceType) {
            SourceOllama -> {
                val url = URL(ollamaURL)

                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 3000
                connection.readTimeout = 30000
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")

                connection.doOutput = true

                val jsonInputString = """
                    {
                        "model": "$modelName",
                        "prompt": "${queryStr}",
                        "stream": false,
                        "system":"你是资深代码工程师，这段文本是 $file 文件的一部分，我需要你告诉这段代码的作用，以及给我代码示例。请用中文回答。代码：${queryStr}"
                    }
                    """.trimIndent()
//                println(jsonInputString)
                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(jsonInputString)
                outputStream.flush()

                if (connection.responseCode == 200) {
                    val ins = connection.inputStream

                    val content = StreamUtils.getStringFromStream(ins)
                    if (content.isNotBlank()) {
                        val result = Gson().fromJson(content, OllamaBean::class.java).toModelResult()
                        callBack.onSuccess(result)
                        //                    LocalData.store(queryWord, Gson().toJson(result))
                    } else {
                        callBack.onFail("翻译接口返回为空")
                    }
                } else {
                    callBack.onFail("错误码：${connection.responseCode}\n错误信息：\n${connection.responseMessage}")
                }
                return
            }

            SourceGemini -> {
                val url =
                    URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.0-pro:generateContent?key=$geminiAPIKey")

                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 3000
                connection.readTimeout = 30000
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")

                connection.doOutput = true
                val jsonInputString = """
                    {
                      "contents": [
                        {
                          "parts": [
                            {
                              "text": "你是资深代码工程师，这段文本是 $file 文件的一部分，我需要你告诉这段代码的作用，以及给我代码示例。请用中文回答。代码：${queryStr}"
                            }
                          ]
                        }
                      ],
                      "generationConfig": {
                        "temperature": 1,
                        "topK": 1,
                        "topP": 1,
                        "maxOutputTokens": 2048,
                        "stopSequences": []
                      },
                      "safetySettings": [
                        {
                          "category": "HARM_CATEGORY_HARASSMENT",
                          "threshold": "BLOCK_MEDIUM_AND_ABOVE"
                        },
                        {
                          "category": "HARM_CATEGORY_HATE_SPEECH",
                          "threshold": "BLOCK_MEDIUM_AND_ABOVE"
                        },
                        {
                          "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                          "threshold": "BLOCK_MEDIUM_AND_ABOVE"
                        },
                        {
                          "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                          "threshold": "BLOCK_MEDIUM_AND_ABOVE"
                        }
                      ]
                    }
                    """.trimIndent()

                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(jsonInputString)
                outputStream.flush()

                if (connection.responseCode == 200) {
                    val ins = connection.inputStream

                    val content = StreamUtils.getStringFromStream(ins)
                    if (content.isNotBlank()) {
                        val result = Gson().fromJson(content, GeminiBean::class.java).toModelResult()
                        callBack.onSuccess(result)
                        //                    LocalData.store(queryWord, Gson().toJson(result))
                    } else {
                        callBack.onFail("翻译接口返回为空")
                    }
                } else {
                    callBack.onFail("错误码：${connection.responseCode}\n错误信息：\n${connection.responseMessage}")
                }
                return
            }

            SourceOpenAI -> {
                val url = URL(openAIURL)

                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 3000
                connection.readTimeout = 30000
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $openAIAPIKey")

                connection.doOutput = true
                val jsonInputString = """
                    {
                     "model": "$openAIModelName",
                     "messages": [
                        {"role": "system", "content": "你是资深代码工程师，这段文本是 $file 文件的一部分，我需要你告诉这段代码的作用，以及给我代码示例。请用中文回答。"},
                        {"role": "user", "content": "${queryStr}"}
                     ],
                     "temperature": 0.3
                    }
                    """.trimIndent()

                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(jsonInputString)
                outputStream.flush()

                if (connection.responseCode == 200) {
                    val ins = connection.inputStream

                    val content = StreamUtils.getStringFromStream(ins)
                    if (content.isNotBlank()) {
                        val result = Gson().fromJson(content, OpenAIBean::class.java).toModelResult()
                        callBack.onSuccess(result)
                        //                    LocalData.store(queryWord, Gson().toJson(result))
                    } else {
                        callBack.onFail("翻译接口返回为空")
                    }
                } else {
                    callBack.onFail("错误码：${connection.responseCode}\n错误信息：\n${connection.responseMessage}")
                }
                return
            }

            else -> {
                callBack.onFail("不支持的翻译源类型$sourceType")
                return
            }
        }

    } catch (e: IOException) {
        callBack.onFail("无法访问：\n${e.message}")
    }
}


