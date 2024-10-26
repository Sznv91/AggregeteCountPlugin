plugins {
    java
    id("org.jetbrains.gradle.plugin.idea-ext") version "0.7"
}

tasks {
    named<Wrapper>("wrapper") {
        gradleVersion = "6.9"
        distributionType = Wrapper.DistributionType.ALL
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    repositories {
        maven(url = "https://store.aggregate.digital/repository/maven-public")
    }
    configurations.all {
        resolutionStrategy {
            setForcedModules("commons-codec:commons-codec:1.6")
        }
        exclude(group = "xerces", module = "xercesImpl")
        exclude(group = "xerces", module = "xmlParserAPIs")
    }
    tasks.withType<JavaCompile> {
        options.isFork = true
        options.isIncremental = true
        options.isWarnings = false
    }
}