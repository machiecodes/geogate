plugins {
    // https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/
    id("fabric-loom") version("1.9.2")
}

base {
    archivesName = project.property("archives_base_name") as String
    version = project.property("mod_version") as String
    group = project.property("maven_group") as String
}

repositories {
    mavenCentral()
}

dependencies {
    // Minecraft
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")

    // Fabric
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    // Jetty
    implementation("org.eclipse.jetty:jetty-server:${project.property("server_version")}")
    implementation("org.eclipse.jetty.ee10:jetty-ee10-webapp:${project.property("webapp_version")}")
    implementation("org.eclipse.jetty.ee10.websocket:jetty-ee10-websocket-jetty-server:${project.property("socket_version")}")

}

tasks {
    processResources {
        val properties = mapOf(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version")
        )

        inputs.properties(properties)

        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}