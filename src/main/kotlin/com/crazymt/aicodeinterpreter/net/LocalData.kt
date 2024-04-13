package com.crazymt.aicodeinterpreter.net

import org.jetbrains.annotations.NonNls
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

const val PREFIX_NAME = "aicodeinterpreter"

object LocalData {
    private val f = File(System.getProperty("user.home") + "/.aicodeinterpreter.properties")
    private val p = Properties()
    private var isInitialized = false
    fun store(@NonNls key: String, @NonNls value: String) {
        p[uncapitalize("${PREFIX_NAME}_$key")] = value
        save()
    }

    init {
        if (!f.exists()) {
            f.createNewFile()
        }
        if (f.exists()) {
            p.load(FileReader(f))
            isInitialized = true
        }
    }


    private fun save() {
        if (isInitialized) {
            p.store(FileWriter(f), "")
        }
    }

    fun read(@NonNls key: String): String? = if (isInitialized) p.getProperty(uncapitalize("${PREFIX_NAME}_$key")) else null

    fun uncapitalize(str: String?): String {
        var strLen: Int = 0
        return if (str != null && (str.length.also { strLen = it }) != 0) StringBuffer(strLen)
            .append(
                str[0].lowercaseChar()
            ).append(str.substring(1)).toString() else str!!
    }

}
