plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.2.2"
}

group = "org.examplee"
version = "2.0.0"
description = "CustomDiscs — свои пластинки под ключ: импорт песен, текстурпак и реестр в одном плагине"
base {
    archivesName.set("CustomDiscs")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.+")
    // Для распаковки tar.xz при автоскачивании ffmpeg
    implementation("org.apache.commons:commons-compress:1.27.1")
    implementation("org.tukaani:xz:1.10")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    archiveFileName.set("CustomDiscs.jar")
    // Убираем в отдельные пакеты, чтобы не конфликтовать с сервером
    relocate("org.apache.commons.compress", "org.examplee.customdiscs.libs.compress")
    relocate("org.tukaani.xz", "org.examplee.customdiscs.libs.xz")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
