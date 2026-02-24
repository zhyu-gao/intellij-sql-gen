plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.11.0"
}

group = "com.plugins.sqlgen"
version = "0.0.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2025.3.3")
        bundledPlugin("com.intellij.database")
        instrumentationTools()
        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    pluginConfiguration {
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "253"
            untilBuild = "263.*"
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    // 发布到 JetBrains Marketplace
    publishPlugin {
        // 从环境变量获取 Token，或者在这里硬编码（不推荐）
        token = System.getenv("JETBRAINS_TOKEN") ?: ""
        
        // 可选：指定发布的渠道，默认为 "default"
        // channels = listOf("default")
        
        // 可选：添加发布说明
        // changelog = file("CHANGELOG.md").readText()
    }
}
