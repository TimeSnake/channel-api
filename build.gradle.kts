plugins {
  id("java")
  id("java-base")
  id("java-library")
  id("maven-publish")
}


group = "de.timesnake"
version = "5.0.0"
var projectId = 22

repositories {
  mavenLocal()
  mavenCentral()
  maven {
    url = uri("https://git.timesnake.de/api/v4/groups/7/-/packages/maven")
    name = "timesnake"
    credentials(PasswordCredentials::class)
  }
}

dependencies {
  compileOnly("de.timesnake:library-basic:2.+")

  compileOnly("org.jetbrains:annotations:23.0.0")
  compileOnly("org.apache.logging.log4j:log4j-api:2.22.1")
  compileOnly("org.apache.logging.log4j:log4j-core:2.22.1")

  testImplementation("de.timesnake:library-basic:2.+")
  testImplementation("net.kyori:adventure-api:4.11.0")

  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
  testImplementation("org.apache.logging.log4j:log4j-api:2.22.1")
  testImplementation("org.apache.logging.log4j:log4j-core:2.22.1")
}

configurations.configureEach {
  resolutionStrategy.dependencySubstitution {
    if (project.parent != null) {
      substitute(module("de.timesnake:library-basic")).using(project(":libraries:library-basic"))
    }
  }
}

publishing {
  repositories {
    maven {
      url = uri("https://git.timesnake.de/api/v4/projects/$projectId/packages/maven")
      name = "timesnake"
      credentials(PasswordCredentials::class)
    }
  }

  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
    }
  }
}

tasks {
  compileJava {
    options.encoding = "UTF-8"
    options.release = 21
  }
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
  withSourcesJar()
}

tasks.test {
  useJUnitPlatform()
}