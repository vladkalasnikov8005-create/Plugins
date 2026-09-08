plugins {
    `java-library`
}

group = "org.examplee"
version = "1.3.1"
description = "LeperClass — класс Прокажённого (заражение, чума, зонт, благословение)"
base {
    archivesName.set("LeperClass")
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
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    archiveFileName.set("LeperClass.jar")
}
