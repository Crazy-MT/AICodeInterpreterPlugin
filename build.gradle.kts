import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.builtins.StandardNames.FqNames.target
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.5.30"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.1.6"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "1.3.0"
    // Gradle Qodana Plugin
    id("org.jetbrains.qodana") version "0.1.12"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    maven {
        setUrl("https://maven.aliyun.com/nexus/content/groups/public/")
        setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    mavenCentral()
    gradlePluginPortal()
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.4.0")
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.10.0")
    implementation("com.vladsch.flexmark:flexmark:0.62.2")
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    downloadSources.set(properties("platformDownloadSources").toBoolean())
    updateSinceUntilBuild.set(true)

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
    reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
    saveReport.set(true)
    showReport.set(System.getenv("QODANA_SHOW_REPORT").toBoolean())
}

tasks.register<proguard.gradle.ProGuardTask>("proguard") {
    verbose()
//    keepdirectories()// By default, directory entries are removed.
    ignorewarnings()
    target("11")

    // Alternatively put your config in a separate file
    configuration("proguard-rules.pro")

    // Use the jar task output as a input jar. This will automatically add the necessary task dependency.
    injars(tasks.named("jar"))

    outjars(tasks.named("jar").get().outputs.files.singleFile.absolutePath.replace(".jar", "-obfuscated.jar"))

    val javaHome = System.getProperty("java.home")
    // Automatically handle the Java version of this build, don't support JBR
    // As of Java 9, the runtime classes are packaged in modular jmod files.
//        libraryjars(
//            // filters must be specified first, as a map
//            mapOf("jarfilter" to "!**.jar",
//                  "filter"    to "!module-info.class"),
//            "$javaHome/jmods/java.base.jmod"
//        )

    // Add all JDK deps
    if( ! properties("skipProguard").toBoolean()) {
        File("$javaHome/jmods/").listFiles()!!.forEach { libraryjars(it.absolutePath) }
    }

//    libraryjars(configurations.runtimeClasspath.get().files)
//    val ideaPath = getIDEAPath()

    // Add all java plugins to classpath
//    File("$ideaPath/plugins/java/lib").listFiles()!!.forEach { libraryjars(it.absolutePath) }
    // Add all IDEA libs to classpath
//    File("$ideaPath/lib").listFiles()!!.forEach { libraryjars(it.absolutePath) }

    libraryjars(configurations.compileClasspath.get())

    dontshrink()
    dontoptimize()

//    allowaccessmodification() //you probably shouldn't use this option when processing code that is to be used as a library, since classes and class members that weren't designed to be public in the API may become public

    adaptclassstrings("**.xml")
    adaptresourcefilecontents("**.xml")// or   adaptresourcefilecontents()

    // Allow methods with the same signature, except for the return type,
    // to get the same obfuscation name.
    overloadaggressively()
    // Put all obfuscated classes into the nameless root package.
//    repackageclasses("")

    printmapping("build/proguard-mapping.txt")

    adaptresourcefilenames()
    optimizationpasses(9)
    allowaccessmodification()
    mergeinterfacesaggressively()
    renamesourcefileattribute("SourceFile")
    keepattributes("Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod")

//    keep("""class org.jetbrains.plugins.template.MyBundle
//    """.trimIndent())
//
//    keep("""class beansoft.mykeep.**
//    """.trimIndent())
//    keep("class beansoft.mykeep.**{*;}")
}


tasks {

    prepareSandbox {
        if( !properties("skipProguard").toBoolean()) {
            dependsOn("proguard")
            doFirst {
                val original = File("${buildDir.absoluteFile.absolutePath}/libs/${project.name}-$version.jar")
                val obfuscated =  File("${buildDir.absoluteFile.absolutePath}/libs/${project.name}-$version-obfuscated.jar")
                if (original.exists() && obfuscated.exists()) {
                    original.delete()
                    obfuscated.renameTo(original)
                    println("plugin file obfuscated")
                } else {
                    println("error: some file does not exist, plugin file not obfuscated")
                }
            }
        }

    }


    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            changelog.run {
                getOrNull(properties("pluginVersion")) ?: getLatest()
            }.toHTML()
        })
    }

    runPluginVerifier {
        ideVersions.set(properties("pluginVerifierIdeVersions").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}
