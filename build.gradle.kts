plugins {
    java
    //kotlin("jvm") version Config.Versions.kotlin
    kotlin("multiplatform") version "1.3.70"
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
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
            implementation("org.evoleq:mathcat-core-jvm:1.0.0")
            implementation("org.evoleq:mathcat-morphism-jvm:1.0.0")
        }
    }
    // JVM-specific tests and their dependencies:
    jvm().compilations["test"].defaultSourceSet {
        dependencies {
            implementation(kotlin("test-junit"))
            implementation("org.evoleq:mathcat-core-jvm:1.0.0")
            implementation("org.evoleq:mathcat-morphism-jvm:1.0.0")
        }
    }

    js().compilations["main"].defaultSourceSet  {
        dependencies {
            //implementation(kotlin("js"))
            //implementation(kotlin("reflect"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.5")
            implementation("org.evoleq:mathcat-core-js:1.0.0")
            implementation("org.evoleq:mathcat-morphism-js:1.0.0")
        }
        /* ... */
    }
    js().compilations["test"].defaultSourceSet {/* ... */ }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation( project(":dynamics") )
                implementation( project(":configurations") )

                implementation("org.evoleq:mathcat-core-metadata:1.0.0")
                implementation("org.evoleq:mathcat-morphism-metadata:1.0.0")

                //implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.5")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}


/*
dependencies {
    implementation( Config.Dependencies.kotlinStandardLibrary )
    implementation( Config.Dependencies.coroutines )
    //implementation( kotlin("reflect") )

    implementation( project(":dynamics") )
    implementation( project(":configurations") )

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.2")
    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
tasks {

    val sourceSets: SourceSetContainer by project

    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        classifier = "sources"
        from(sourceSets["main"].allSource)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
        classifier = "javadoc"
        from(getByName("javadoc"))
    }


    val dokkaJar by creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        classifier = "javadoc"
        from(getByName("dokka"))
    }

    artifacts {
        add("archives", sourcesJar)
        add("archives", dokkaJar)
    }


}

task("writeNewPom") {
    doLast {
        maven.pom {
            withGroovyBuilder {
                "project" {
                    // setProperty("inceptionYear", "2008")
                    "licenses" {
                        "license" {
                            setProperty("name", "The Apache Software License, Version 2.0")
                            setProperty("url", "http://www.apache.org/licenses/LICENSE-2.0.txt")
                            setProperty("distribution", "repo")
                        }
                    }
                }
            }
        }.writeTo("$buildDir/pom.xml")
    }
}

publishing {
    publications {
        create<MavenPublication>("EvoleqPublication"){
            artifactId = Config.ProjectData.Core.artifactId
            groupId = Config.ProjectData.group
            from (components["java"])

            artifact (tasks.getByName("sourcesJar")) {
                classifier = "sources"
            }

            artifact (tasks.getByName("javadocJar")) {
                classifier = "javadoc"
            }

            pom.withXml {
                val root = asNode()
                root.appendNode("description", "A declarative approach to application design based on the theory of dynamical systems")
                root.appendNode("name", Config.ProjectData.Core.artifactId)
                root.appendNode("url", "https://github.com/doctor-smith/evoleq.git")
                root.children().addAll(maven.pom().dependencies)
            }

            pom {
                developers{
                    developer{
                        id.set("drx")
                        name.set("Dr. Florian Schmidt")
                        email.set("schmidt@alpha-structure.com")
                    }
                }
            }

        }
    }
}




bintray {
    user = project.properties["bintray.user"] as String
    key = project.properties["bintray.key"] as String

    publish = true
    override = true


    pkg (delegateClosureOf<BintrayExtension.PackageConfig>{
        repo = "maven"
        name = "evoleq-core"
        description = "A declarative approach to application design using the theory of dynamical systems"
        //userOrg = user
        vcsUrl = "https://bitbucket.org/dr-smith/evoleq.git"
        setLabels("kotlin", "coroutine", "dynamical system", "recursive store", "evolution equation", "declarative", "functional")
        setLicenses("Apache-2.0")

        version (delegateClosureOf<BintrayExtension.VersionConfig>{
            name = Config.ProjectData.Core.version
            //desc = "build ${build.number}"
            //released  = Date(System.currentTimeMillis())
            gpg (delegateClosureOf<BintrayExtension.GpgConfig>{
                sign = true
            })
        })
    })

}
*/
//apply<org.drx.evoleq.plugin.EvoleqPlugin>()




