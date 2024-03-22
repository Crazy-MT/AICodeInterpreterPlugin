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
    private val etOllamaURL = JBTextField("http://localhost:11434/api/generate")
    private val etModelName = JBTextField() // gemma:2b
    private val etGemini = JBTextField()

    private val rbOllama = JRadioButton("Ollama 翻译配置");
    private val rbGemini = JRadioButton("Gemini 翻译配置");

    private val buttonGroup = ButtonGroup();

    init {
        etOllamaURL.text = "http://localhost:11434/api/generate"
        buttonGroup.add(rbOllama)
        buttonGroup.add(rbGemini)

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

        buttonGroup.setSelected(rbGemini.model, true)

        panel = FormBuilder.createFormBuilder()
            .addSeparator()
            .addComponent(rbOllama)
            .addComponent(
                JBLabel(
                    "请安装 https://ollama.com/ 及本地大模型后使用",
                    ComponentStyle.SMALL,
                    UIUtil.FontColor.BRIGHTER
                )
            )
            .addLabeledComponent(JBLabel("API URL"), etOllamaURL, 1, false)
            .addLabeledComponent(JBLabel("API 模型"), etModelName, 1, false)
            .addSeparator()
            .addComponent(rbGemini)
            .addComponent(
                JBLabel(
                    "申请 apikey https://aistudio.google.com/app/apikey",
                    ComponentStyle.SMALL,
                    UIUtil.FontColor.BRIGHTER
                )
            )
            .addLabeledComponent(JBLabel("api key"), etGemini, 1, false)
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
            return SourceGemini
        }
        set(newText) {
            if (newText == SourceOllama) {
                buttonGroup.setSelected(rbOllama.model, true)
            }
            if (newText == SourceGemini) {
                buttonGroup.setSelected(rbGemini.model, true)
            }
        }
}