plugins {
    java
    //library
    //kotlin("jvm") version Config.Versions.kotlin
    kotlin("multiplatform") version Config.Versions.kotlin
    id ("com.github.hierynomus.license") version "0.15.0"
    `maven-publish`
    maven
    id ("com.jfrog.bintray") version "1.8.0"
    id("org.jetbrains.dokka") version "0.9.17"
}

group = Config.ProjectData.group
version = Config.ProjectData.Core.version//+"-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}


kotlin {
    /* Targets configuration omitted.
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */
    
    jvm().compilations["main"].defaultSourceSet {
        dependencies {
            implementation(kotlin("stdlib-jdk8"))
            //implementation(kotlin("reflect"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Config.Versions.coroutines}")
            implementation("org.evoleq:mathcat-core-jvm:${Config.Versions.mathcatMain}")
            implementation("org.evoleq:mathcat-morphism-jvm:${Config.Versions.mathcatMain}")
        }
    }
    // JVM-specific tests and their dependencies:
    jvm().compilations["test"].defaultSourceSet {
        dependencies {
            implementation(kotlin("test-junit"))
            //implementation(kotlin("test"))
            implementation("org.evoleq:mathcat-core-jvm:${Config.Versions.mathcatMain}")
            implementation("org.evoleq:mathcat-morphism-jvm:${Config.Versions.mathcatMain}")
        }
    }

    js().compilations["main"].defaultSourceSet  {
        dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${Config.Versions.coroutines}")
            implementation("org.evoleq:mathcat-core-js:${Config.Versions.mathcatMain}")
            implementation("org.evoleq:mathcat-morphism-js:${Config.Versions.mathcatMain}")
        }
    }
    js().compilations["test"].defaultSourceSet {
        dependencies {
            //implementation("org.evoleq:dynamics-js:2.0.0-alpha")
            //implementation("org.evoleq:configurations-js:2.0.0-alpha")
            implementation(kotlin("test-js"))
            implementation(kotlin("test-js-runner"))
            implementation("org.evoleq:mathcat-core-js:${Config.Versions.mathcatMain}")
            implementation("org.evoleq:mathcat-morphism-js:${Config.Versions.mathcatMain}")
        }
    }
    js{
        browser()
        //nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation( project(":dynamics") )
                implementation( project(":configurations") )

                implementation("org.evoleq:mathcat-core:${Config.Versions.mathcatMain}")
                implementation("org.evoleq:mathcat-morphism:${Config.Versions.mathcatMain}")

                //implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Config.Versions.coroutines}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation( project(":dynamics") )
                implementation( project(":configurations") )
                implementation( project(":mpp-test") )
                implementation("org.evoleq:mathcat-core:${Config.Versions.mathcatMain}")
                implementation("org.evoleq:mathcat-morphism:${Config.Versions.mathcatMain}")
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

apply(from = "../publish.mpp.gradle.kts")