val jarImplementation by configurations.creating

sourceSets {
    main {
        java {
            srcDir("src/main/java")
        }
        resources {
            srcDir("src/main/java")
            exclude("**/*.java")
        }
    }
    test {
        java {
            srcDir("src/test/java")
        }
    }
}

dependencies {
    api(ApacheCommonsLibs.commonsNet)
    api(ApacheCommonsLibs.commonsBeanutils)
    api(ApacheCommonsLibs.commonsLogging)
    api(ApacheCommonsLibs.commonsLang3)
    api(ApacheCommonsLibs.commonsIo)
    api(ApacheCommonsLibs.commonsMath3)

    api(Log4JLibs.log4jApi)
    api(Log4JLibs.log4jCore)
    api(Log4JLibs.log4j12Api)
    api(Log4JLibs.log4jSlf4jImpl)
    api(Log4JLibs.slf4jApi)

    api("net.sourceforge.javacsv", "javacsv", "2.1")
    api("xalan", "xalan", "2.7.2")
    api("com.googlecode.json-simple", "json-simple", "1.1.1") {
        exclude(group = "junit", module = "junit")
    }

    api("com.google.code.findbugs", "jsr305", "3.0.2")       // @Nonnull and @Nullable annotations
    api(GoogleLibs.guava)

    api("net.sf.jpf", "jpf", "1.5.1")
}

tasks.named("assemble") {
    dependsOn(":widget-api:assemble")
}

tasks.withType<Jar> {
    from(jarImplementation.asFileTree.files.map { zipTree(it) })
    from("../widget-api/build/classes/java/main")
    from("../widget-api/build/classes/java/test") {
        exclude("Test*")
    }
    from("../widget-api/build/resources/main")
    exclude("**/*.jar")
}