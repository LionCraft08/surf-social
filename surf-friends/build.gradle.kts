import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("java")
    id("com.gradleup.shadow")
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }

    mavenLocal()
}


dependencies {
    implementation(libs.kotlin.jvm)
    compileOnlyApi(libs.paper.api)
    compileOnly(libs.commandapi.bukkit)
    compileOnly(libs.packetevents.api)
    implementation(libs.inventory.framework)

    implementation ("com.zaxxer:HikariCP:5.0.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    compileOnly("dev.slne.surf:surf-gui-api:2.0.3-SNAPSHOT")
    implementation("dev.hsbrysk:caffeine-coroutines:1.2.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.20.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.20.0")
    implementation("mysql:mysql-connector-java:8.0.33")

    implementation("net.wesjd:anvilgui:1.10.3-SNAPSHOT")
}

paper {
    main = "dev.slne.surf.friends.SurfFriendsPlugin"
    apiVersion = "1.21"
    authors = listOf("TheBjoRedCraft", "SLNE Development")
    prefix = "SurfSocial/SurfFriends"
    name = "SurfFriends"
    version = "3.1.0-SNAPSHOT"

    serverDependencies {
        register("CommandAPI") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
        }
        register("surf-gui-bukkit") {
            load = PaperPluginDescription.RelativeLoadOrder.AFTER
            required = true
            joinClasspath = true
        }
    }
}

tasks.shadowJar {
    relocate("com.github.stefvanschie.inventoryframework", "dev.slne.surf.friends.inventoryframework")
    relocate("kotlin", "dev.slne.surf.shaded.kotlin")
    archiveClassifier.set("")
    archiveVersion.set("3.1.0-SNAPSHOT")
    archiveBaseName.set("surf-friends")

    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot"
    }
}
kotlin {
    jvmToolchain(21)
}