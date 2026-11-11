// :everyconfig — companion mod for hellow.
//
// Embeds third-party config-library screens (Cloth Config, YACL, ...) into a
// hellow page as a normal element. Every config library is a SOFT dependency:
// it is compileOnly here (so adapters are type-checked against the real API)
// and each adapter gates itself with a runtime ModList check, so everyconfig
// runs fine when the library mod is not installed (the embed shows a
// "not installed" placeholder instead).

plugins {
    id("net.neoforged.moddev").version("2.0.141")
}

neoForge {
    version = "21.1.241"

    parchment {
        mappingsVersion.set("2024.11.17")
        minecraftVersion.set("1.21.1")
    }

    mods {
        create("everyconfig") {
            sourceSet(sourceSets.main.get())
        }
    }
}

version = "1.0.0"
group = "com.everyconfig"

repositories {
    // Cloth Config
    maven("https://maven.shedaniel.me/") { name = "Shedaniel Maven" }
    // YACL
    maven("https://maven.isxander.dev/releases") { name = "Xander Maven" }
}

dependencies {
    // Hellow is a separate mod: compile against its jar, resolve it at runtime
    // through the NeoForge mod dependency declared in neoforge.mods.toml.
    compileOnly(files("libs/hellow-1.0.0.jar"))

    // Soft (compile-time only) config-library dependencies. Not bundled; each
    // adapter checks ModList before touching these classes.
    compileOnly("me.shedaniel.cloth:cloth-config-neoforge:15.0.140")
    compileOnly("dev.isxander:yet-another-config-lib:3.7.1+1.21.1-neoforge")
    // A real mod whose config screen is built with Cloth Config — its adapter
    // injects the mod's actual config screen (REI's REI Settings), not a demo.
    compileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:16.0.788")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    // DemoConfigStore (and its tests) persist JSON via Gson; Gson is a runtime dep
    // of Minecraft at runtime, but the bare JUnit test JVM has to bring its own.
    testImplementation("com.google.code.gson:gson:2.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
    testRuntimeOnly("org.slf4j:slf4j-api:2.0.9")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
