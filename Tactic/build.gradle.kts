plugins {
    `java-library`
}

group = "org.examplee"
version = "1.9"
description = "Tactic — маска, боевые предметы, алмазный прессинг, регионы (Paper 26.2)"
base {
    archivesName.set("Tactic")
}

java {
    // Paper 26.2 требует Java 25
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.+")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

val projectVersion = version.toString()

tasks.processResources {
    inputs.property("pluginVersion", projectVersion)
    expand("version" to projectVersion)
}

tasks.jar {
    archiveFileName.set("Tactic.jar")
}
