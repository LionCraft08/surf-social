import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

val versionFile = file("version.txt")
val incrementVersion = project.findProperty("incrementVersion")?.toString()?.toBoolean() ?: true

val versionParts = versionFile.readText().trim().split(".")
val major = versionParts[0].toInt()
val minor = versionParts[1].toInt()
var patch = versionParts[2].toInt()

if (incrementVersion) {
    patch += 1
}

val newVersion = "$major.$minor.$patch"

group = "dev.slne"
version = "$newVersion-1.21.4-SNAPSHOT"
repositories {
    mavenCentral()
    mavenLocal()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        name = "codemc"
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.extendedclip.com/releases/")
    }
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.11.6")
    //compileOnly(files("run/plugins/surf-gui-bukkit-2.0.3-SNAPSHOT-all.jar"))
    compileOnly("dev.slne.surf:surf-gui-api:2.0.3-SNAPSHOT")

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("dev.jorel:commandapi-bukkit-kotlin:9.7.0")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.social.chat.SurfChat")
    authors.add("SLNE Development")

    generateLibraryLoader(false)
    serverDependencies{
        register("surf-gui-bukkit") {
            load = PaperPluginDescription.RelativeLoadOrder.AFTER
            required = true
            joinClasspath = true
        }
    }
}


tasks {
    shadowJar {
        archiveFileName = "${project.name}-${project.version}.jar"

        if (incrementVersion) {
            dependsOn("incrementVersion")
        }
    }
}

tasks.register("incrementVersion") {
    doLast {
        versionFile.writeText(newVersion)
        println("Incremented version to $newVersion")
    }
}

kotlin {
    jvmToolchain(21)
}