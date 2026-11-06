plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.21"
    id("org.jetbrains.intellij.platform") version "2.11.0"
}

group = "com.plugins.sqlgen"
version = "0.0.6"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // 编译基线取"最低支持版本"而非最新版本：
        // 插件声明 since-build 253，若用 2026.3（263）编译，Kotlin 会把 FileEditorManager.getInstance
        // 绑定到 263 新增的 FileEditorManager.Companion，在 253/261 上抛 NoSuchFieldError。
        // 同时 263 平台字节码为 Java 25（class 69.0），JDK 21 的 javac 无法读取，Java 源码也无法参与编译。
        // 最新版本（2026.3）由下方 pluginVerification 负责验证。
        intellijIdea("2025.3")
        bundledPlugin("com.intellij.database")
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

    // 插件兼容性验证目标 IDE（verifyPlugin 依赖该配置）
    pluginVerification {
        ides {
            create(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaUltimate, "2026.3")
            create(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaUltimate, "2025.3")
            // 如需覆盖全部 since-build 区间，替换为 recommended()
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
    // publishPlugin {
    //     token = System.getenv("JETBRAINS_TOKEN") ?: ""
    //     channels = listOf("default")
    //     changelog = file("CHANGELOG.md").readText()
    // }
    // 发布到 JetBrains Marketplace
    publishPlugin {
        // 从环境变量获取 Token，或者在这里硬编码（不推荐）
        token = System.getenv("JETBRAINS_TOKEN") ?: ""
        
        // 可选：指定发布的渠道，默认为 "default"
//        channels = listOf("default")
        
        // 可选：添加发布说明
//        changelog = file("CHANGELOG.md").readText()
    }
}
