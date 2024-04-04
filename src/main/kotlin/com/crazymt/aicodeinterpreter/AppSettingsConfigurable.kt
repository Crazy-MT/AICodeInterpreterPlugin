package com.crazymt.aicodeinterpreter

import AppSettingsComponent
import com.crazymt.aicodeinterpreter.net.*
import com.intellij.openapi.options.Configurable
import org.apache.tools.ant.taskdefs.Sleep
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.Nullable
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
internal class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "AICodeInterpreter"
    }

    @Nullable
    override fun createComponent(): JComponent? {
        mySettingsComponent = AppSettingsComponent()
        return mySettingsComponent?.panel
    }
    ///判断是否调用apply
    override fun isModified(): Boolean {
        val sourceType = LocalData.read("sourceType")
        val ollamaURL = LocalData.read("ollamaURL")
        val modelName = LocalData.read("modelName")
        val geminiAPIkey = LocalData.read("geminiAPIKey")

        val openAIURL = LocalData.read("openAIURL")
        val openAIModelName = LocalData.read("openAIModelName")
        val openAIAPIKey = LocalData.read("openAIAPIKey")

        var modified: Boolean = !mySettingsComponent?.sourceType.equals(sourceType)

        modified = modified
                || !mySettingsComponent?.ollamaURL.equals(ollamaURL)
                || !mySettingsComponent?.modelName.equals(modelName)
                || !mySettingsComponent?.geminiAPIKey.equals(geminiAPIkey)
                || !mySettingsComponent?.openAIURL.equals(openAIURL)
                || !mySettingsComponent?.openAIModelName.equals(openAIModelName)
                || !mySettingsComponent?.openAIAPIKey.equals(openAIAPIKey)
        return modified
    }
    ///保存设置
    override fun apply() {
        var _sourceType = mySettingsComponent?.sourceType ?: SourceGemini

        val _ollamaURL = mySettingsComponent?.ollamaURL ?: "http://localhost:11434/api/generate"
        val _modelName = mySettingsComponent?.modelName ?: ""
        val _geminiAPIKey = mySettingsComponent?.geminiAPIKey ?: ""

        val _openAIURL = mySettingsComponent?.openAIURL ?:"https://api.openai.com/v1/chat/completions"
        val _openAIModelName = mySettingsComponent?.openAIModelName ?:"gpt-3.5-turbo"
        val _openAIAPIKey =  mySettingsComponent?.openAIAPIKey ?:""

        LocalData.store("sourceType", _sourceType)
        LocalData.store("ollamaURL", _ollamaURL)
        LocalData.store("modelName", _modelName)
        LocalData.store("geminiAPIKey", _geminiAPIKey)
        LocalData.store("openAIURL", _openAIURL)
        LocalData.store("openAIModelName", _openAIModelName)
        LocalData.store("openAIAPIKey", _openAIAPIKey)

        sourceType = _sourceType
        ollamaURL = _ollamaURL
        modelName = _modelName
        geminiAPIKey = _geminiAPIKey
        openAIURL = _openAIURL
        openAIModelName = _openAIModelName
        openAIAPIKey = _openAIAPIKey

        ChatCompletionAPI.newSessionId;

    }

    override fun reset() {
        val sourceType = LocalData.read("sourceType")
        val ollamaURL = LocalData.read("ollamaURL")
        val modelName = LocalData.read("modelName")
        val geminiAPIKey = LocalData.read("geminiAPIKey")

        val openAIURL = LocalData.read("openAIURL")
        val openAIModelName = LocalData.read("openAIModelName")
        val openAIAPIKey = LocalData.read("openAIAPIKey")

        mySettingsComponent?.let {
            it.sourceType = sourceType ?: ""
            it.ollamaURL = ollamaURL ?: "http://localhost:11434/api/generate"
            it.modelName = modelName ?: ""
            it.geminiAPIKey = geminiAPIKey ?: ""
            it.openAIURL = openAIURL ?: "https://api.openai.com/v1/chat/completions"
            it.openAIModelName = openAIModelName ?: "gpt-3.5-turbo"
            it.openAIAPIKey = openAIAPIKey ?: ""
        }
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}
