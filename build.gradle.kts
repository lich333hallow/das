import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
  id("xyz.jpenilla.run-paper") version "2.3.1"
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0"
}

group = "io.papermc.paperweight"
version = "1.0.0-SNAPSHOT"
description = "Test plugin for paperweight-userdev (edited)"

java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.assemble {
  dependsOn(tasks.reobfJar)
}

dependencies {
  paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
}

tasks {
  compileJava {
    options.release = 21
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }
}
bukkitPluginYaml {
  main = "io.papermc.paperweight.testplugin.TestPlugin"
  load = BukkitPluginYaml.PluginLoadOrder.STARTUP
  authors.add("lich333hallow")
  apiVersion = "1.21.5"
}
