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
    /*
    js{
        
        
        browser{
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
      
        
    }
    
    
     */
    jvm().compilations["main"].defaultSourceSet {
        dependencies {
            implementation(kotlin("stdlib-jdk8"))
            //implementation(kotlin("reflect"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Config.Versions.coroutines}")
            implementation("org.evoleq:mathcat-core-jvm:1.0.1")
            implementation("org.evoleq:mathcat-morphism-jvm:1.0.1")
        }
    }
    // JVM-specific tests and their dependencies:
    jvm().compilations["test"].defaultSourceSet {
        dependencies {
            implementation(kotlin("test-junit"))
            //implementation(kotlin("test"))
            implementation("org.evoleq:mathcat-core-jvm:1.0.1")
            implementation("org.evoleq:mathcat-morphism-jvm:1.0.1")
        }
    }

    js().compilations["main"].defaultSourceSet  {
        dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:${Config.Versions.coroutines}")
            implementation("org.evoleq:mathcat-core-js:1.0.1")
            implementation("org.evoleq:mathcat-morphism-js:1.0.1")
        }
    }
    js().compilations["test"].defaultSourceSet {
        dependencies {
            //implementation("org.evoleq:dynamics-js:2.0.0-alpha")
            //implementation("org.evoleq:configurations-js:2.0.0-alpha")
            implementation(kotlin("test-js"))
            implementation(kotlin("test-js-runner"))
            implementation("org.evoleq:mathcat-core-js:1.0.1")
            implementation("org.evoleq:mathcat-morphism-js:1.0.1")
        }
    }
    js{
        browser()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation( project(":dynamics") )
                implementation( project(":configurations") )

                implementation("org.evoleq:mathcat-core:1.0.1")
                implementation("org.evoleq:mathcat-morphism:1.0.1")

                //implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Config.Versions.coroutines}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation( project(":dynamics") )
                implementation( project(":configurations") )
                implementation( project(":mpp-test") )
                implementation("org.evoleq:mathcat-core:1.0.1")
                implementation("org.evoleq:mathcat-morphism:1.0.1")
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

apply(from = "../publish.mpp.gradle.kts")