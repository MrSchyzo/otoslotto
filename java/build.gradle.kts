import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    application
    id("me.champeau.jmh") version "0.7.3"
}

application {
    mainClass = "com.mrschyzo.hungarian.App"
}

group = "com.mrschyzo"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(26))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--add-modules=jdk.incubator.vector")
}

tasks.withType<Test>().configureEach {
    jvmArgs("--add-modules=jdk.incubator.vector")
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--add-modules=jdk.incubator.vector")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

jmh {
    jvmArgs.add("--add-modules=jdk.incubator.vector")
    jvmArgs.add("-XX:StartFlightRecording=filename=./profiling-results/app.jfr,settings=profile,maxsize=50M,maxage=30m,dumponexit=true")
}

tasks.test {
    maxHeapSize = "2g"

    useJUnitPlatform()

    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }

    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.STANDARD_ERROR, TestLogEvent.STANDARD_OUT, TestLogEvent.PASSED)
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
    }

    afterSuite(
        KotlinClosure2<TestDescriptor, TestResult, Unit>({ testDescriptor, testResult ->
            if (testDescriptor.parent == null) {
                with(testResult) {
                    logger.lifecycle("Tests run $testCount. Failures: $failedTestCount, Skipped: $skippedTestCount.")
                }
            }
        }),
    )
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.tngtech.archunit:archunit:1.5.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}
