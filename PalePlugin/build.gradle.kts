plugins {
    `java-library`
}

group = "org.examplee"
version = "1.14"
description = "PalePlugin — разрастание бледного леса (заражение мира, предметы очищения, контракты)"
base {
    archivesName.set("PalePlugin")
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
    archiveFileName.set("PalePlugin.jar")
}
