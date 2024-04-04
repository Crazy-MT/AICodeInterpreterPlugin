package com.crazymt.aicodeinterpreter.bean

import com.crazymt.aicodeinterpreter.net.SourceGemini
import com.crazymt.aicodeinterpreter.net.SourceOllama
import com.crazymt.aicodeinterpreter.net.SourceOpenAI

data class ModelResult(val source: String, val from: String, val to: String, val src: String, val result: String?, val error: String?) {
    override fun toString(): String {
        return "$source: ${result ?: "error:${error}"}"
    }
}

data class OpenAIBean(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
) {
    fun toModelResult(): ModelResult {
        return ModelResult(SourceOpenAI, SourceOpenAI, choices[0].message.content, choices[0].message.content, choices[0].message.content, "");
    }
}

data class Choice(
    val index: Int,
    val message: Message,
    val finish_reason: String
)

data class Message(
    val role: String,
    val content: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

data class OpenAIToken(
    val persona: String,
    val arkose: Arkose,
    val turnstile: Turnstile,
    val token: String
)

data class Arkose(
    val required: Boolean,
    val dx: Any? // 如果您知道 dx 的类型，请替换为正确的类型
)

data class Turnstile(
    val required: Boolean
)

data class OpenAIStreamBean(
    val message: ChatMessage
)

data class ChatMessage(
    val id: String,
    val author: Author,
    val createTime: Double,
    val updateTime: Double?,
    val content: ChatContent,
    val status: String,
    val endTurn: Boolean,
    val weight: Int,
    val metadata: Metadata,
    val recipient: String,
)

data class Author(
    val role: String,
    val name: String?,
    val metadata: Map<String, Any>
)

data class ChatContent(
    val contentType: String,
    val parts: List<String>
)

data class Metadata(
    val finishDetails: FinishDetails,
    val citations: List<Any>,
    val gizmoId: Any?,
    val is_complete: Boolean,
    val message_type: String,
    val model_slug: String,
    val defaultModelSlug: String,
    val pad: String,
    val parent_id: String
)

data class FinishDetails(
    val type: String,
    val stopTokens: List<Int>
)


data class OllamaRequest(
    val model: String?,
    val prompt: String?,
    val file: String?
) {
    fun toJson(): String {
        return """
            {
                "model": "$model",
                "prompt": "$prompt",
                "stream": false,
                "system": "你是资深代码工程师，这段文本是 ${file ?: "unknown"} 文件的一部分，我需要你告诉这段代码的作用，以及给我代码示例。请用中文回答。代码：$prompt"
            }
        """.trimIndent()
    }
}

data class OpenAIRequest(
    val model: String?,
    val queryWord: String?,
    val file: String?
) {
    fun toJson(): String {
        return """
            {
             "model": "$model",
             "messages": [
                {"role": "system", "content": "你是资深代码工程师，这段文本是 ${file ?: "unknown"} 文件的一部分，我需要你告诉这段代码的作用，以及给我代码示例。请用中文回答。"},
                {"role": "user", "content": "$queryWord"}
             ],
             "temperature": 0.3
            }
        """.trimIndent()
    }
}

/**
 * ollama
 */
data class OllamaBean(val model: String, val response: String) {
    fun toModelResult(): ModelResult {
        return ModelResult(SourceOllama, model, response, response, response, "");
    }
}

/**
 * gemini
 */
data class GeminiRequest(
    val queryWord: String?,
    val file: String?
) {
    fun toJson(): String {
        return """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "你是资深代码工程师，这段文本是 ${file ?: "unknown"} 文件的一部分，我需要你告诉这段代码的作用，以及给我代码示例。请用中文回答。代码：$queryWord"
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
    }
}

data class GeminiBean(
    val candidates: List<Candidate>,
    val promptFeedback: PromptFeedback
) {
    fun toModelResult(): ModelResult {
        return ModelResult(SourceGemini, SourceGemini, candidates[0].content.parts[0].text, candidates[0].content.parts[0].text, candidates[0].content.parts[0].text, "");
    }
}

data class Candidate(
    val content: Content,
    val finishReason: String,
    val index: Int,
    val safetyRatings: List<SafetyRating>
)
data class Content(
    val parts: List<Part>,
    val role: String
)

data class Part(
    val text: String
)

data class SafetyRating(
    val category: String,
    val probability: String
)

data class PromptFeedback(
    val safetyRatings: List<SafetyRating>
)


