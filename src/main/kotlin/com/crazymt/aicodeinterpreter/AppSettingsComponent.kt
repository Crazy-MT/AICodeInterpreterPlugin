import com.crazymt.aicodeinterpreter.net.*
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.UIUtil.ComponentStyle
import org.jetbrains.annotations.NotNull
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JRadioButton


/**
 * Supports creating and managing a [JPanel] for the Settings Dialog.
 */
class AppSettingsComponent {
    val panel: JPanel
    private val rbFreeChatGPT = JRadioButton("Free ChatGPT");

    private val rbOllama = JRadioButton("Ollama Setting");
    private val etOllamaURL = JBTextField("http://localhost:11434/api/generate")
    private val etModelName = JBTextField() // gemma:2b

    private val rbGemini = JRadioButton("Gemini Setting");
    private val etGemini = JBTextField()

    private val rbOpenAI = JRadioButton("OpenAI Setting");
    private val etOpenAIURL = JBTextField()
    private val etOpenAIModelName = JBTextField()
    private val etOpenAIAPIKey = JBTextField()

    private val buttonGroup = ButtonGroup();

    init {
        etOllamaURL.text = "http://localhost:11434/api/generate"
        buttonGroup.add(rbFreeChatGPT)
        buttonGroup.add(rbOllama)
        buttonGroup.add(rbGemini)
        buttonGroup.add(rbOpenAI)

        rbFreeChatGPT.addItemListener {
            val source = it.source as JRadioButton
            if (source.isSelected) {
                sourceType = SourceFreeGPT
            }
        }
        rbOllama.addItemListener {
            val source = it.source as JRadioButton
            if (source.isSelected) {
                sourceType = SourceOllama
            }
        }

        rbGemini.addItemListener {
            val source = it.source as JRadioButton
            if (source.isSelected) {
                sourceType = SourceGemini
            }
        }

        rbOpenAI.addItemListener {
            val source = it.source as JRadioButton
            if (source.isSelected) {
                sourceType = SourceOpenAI
            }
        }

        buttonGroup.setSelected(rbFreeChatGPT.model, true)

        panel = FormBuilder.createFormBuilder()
            .addSeparator()
            .addComponent(rbFreeChatGPT)
            .addComponent(
                JBLabel(
                    "Please ensure that the connection with OpenAI is normal.",
                    ComponentStyle.SMALL,
                    UIUtil.FontColor.BRIGHTER
                )
            )
            .addSeparator()
            .addComponent(rbOllama)
            .addComponent(
                JBLabel(
                    "Please install https://ollama.com/ and local model, then use it.",
                    ComponentStyle.SMALL,
                    UIUtil.FontColor.BRIGHTER
                )
            )
            .addLabeledComponent(JBLabel("API URL"), etOllamaURL, 1, false)
            .addLabeledComponent(JBLabel("API Model Name"), etModelName, 1, false)
            .addSeparator()
            .addComponent(rbGemini)
            .addComponent(
                JBLabel(
                    "To apply for an API key, please visit https://aistudio.google.com/app/apikey",
                    ComponentStyle.SMALL,
                    UIUtil.FontColor.BRIGHTER
                )
            )
            .addLabeledComponent(JBLabel("api key"), etGemini, 1, false)
            .addComponent(rbOpenAI)
            .addComponent(
                JBLabel(
                    "Supports APIs similar to OpenAI, including but not limited to OpenAI, Kimi(Moonshot), DeepSeek... Only API URL, API key, and Model name are required.",
                    ComponentStyle.SMALL,
                    UIUtil.FontColor.BRIGHTER
                )
            )
            .addLabeledComponent(JBLabel("url"), etOpenAIURL, 1, false)
            .addLabeledComponent(JBLabel("model name"), etOpenAIModelName, 1, false)
            .addLabeledComponent(JBLabel("api key"), etOpenAIAPIKey, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    @get:NotNull
    var ollamaURL: String
        get() = etOllamaURL.getText()
        set(newText) {
            if (newText == "") {
                etOllamaURL.text = "http://localhost:11434/api/generate"
            } else {
                etOllamaURL.setText(newText)
            }
            etOllamaURL.setText(newText)
        }

    @get:NotNull
    var modelName: String
        get() = etModelName.getText()
        set(newText) {
            etModelName.setText(newText)
        }

    @get:NotNull
    var geminiAPIKey: String
        get() = etGemini.getText()
        set(newText) {
            etGemini.setText(newText)
        }

    @get:NotNull
    var sourceType: String
        get() {
            if (rbOllama.isSelected) {
                return SourceOllama
            }

            if (rbGemini.isSelected) {
                return SourceGemini
            }

            if (rbOpenAI.isSelected) {
                return SourceOpenAI
            }

            if (rbFreeChatGPT.isSelected) {
                return SourceFreeGPT
            }
            return SourceFreeGPT
        }
        set(newText) {
            if (newText == SourceOllama) {
                buttonGroup.setSelected(rbOllama.model, true)
            }
            if (newText == SourceGemini) {
                buttonGroup.setSelected(rbGemini.model, true)
            }

            if (newText == SourceOpenAI) {
                buttonGroup.setSelected(rbOpenAI.model, true)
            }

            if (newText == SourceFreeGPT) {
                buttonGroup.setSelected(rbFreeChatGPT.model, true)
            }
        }

    @get:NotNull
    var openAIURL: String
        get() = etOpenAIURL.getText()
        set(newText) {
            if (newText == "") {
                etOpenAIURL.text = "https://api.openai.com/v1/chat/completions"
            } else {
                etOpenAIURL.setText(newText)
            }
            etOpenAIURL.setText(newText)
        }

    @get:NotNull
    var openAIModelName: String
        get() = etOpenAIModelName.getText()
        set(newText) {
            etOpenAIModelName.setText(newText)
        }

    @get:NotNull
    var openAIAPIKey: String
        get() = etOpenAIAPIKey.getText()
        set(newText) {
            etOpenAIAPIKey.setText(newText)
        }

}