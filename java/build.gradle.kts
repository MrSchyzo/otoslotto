import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("me.champeau.jmh") version "0.7.3"
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
