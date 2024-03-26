package com.crazymt.aicodeinterpreter.bean

import com.crazymt.aicodeinterpreter.net.SourceGemini
import com.crazymt.aicodeinterpreter.net.SourceOllama
import com.crazymt.aicodeinterpreter.net.SourceOpenAI

data class ModelResult(val source: String, val from: String, val to: String, val src: String, val result: String?, val error: String?) {
    override fun toString(): String {
        return "$source: ${result ?: "error:${error}"}"
    }
}

data class OllamaBean(val model: String, val response: String) {
    fun toModelResult(): ModelResult {
        return ModelResult(SourceOllama, model, response, response, response, "");
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



