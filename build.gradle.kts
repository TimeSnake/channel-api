plugins {
  id("java")
  id("java-base")
  id("java-library")
  id("maven-publish")
}


group = "de.timesnake"
version = "6.0.0"
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
  api("de.timesnake:library-basic:3.+")

  api("org.jetbrains:annotations:23.0.0")
  api("org.apache.logging.log4j:log4j-api:2.22.1")
  api("org.apache.logging.log4j:log4j-core:2.22.1")

  testImplementation("de.timesnake:library-basic:3.+")
  testImplementation("net.kyori:adventure-api:4.11.0")

  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
  testImplementation("org.apache.logging.log4j:log4j-api:2.22.1")
  testImplementation("org.apache.logging.log4j:log4j-core:2.22.1")
}

configurations.all {
  resolutionStrategy.dependencySubstitution.all {
    requested.let {
      if (it is ModuleComponentSelector && it.group == "de.timesnake") {
        val targetProject = findProject(":${it.module}")
        if (targetProject != null) {
          useTarget(targetProject)
        }
      }
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