import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.openapi.generator)
}

android {
    namespace = "com.ginogipsy.sanmartinoapp"
    compileSdk = libs.versions.androidCompileSdk.get().toInt() // Aggiornato ad Android 17 (API 37)

    defaultConfig {
        applicationId = "com.ginogipsy.sanmartinoapp"
        minSdk = 36 // Puoi mantenere questo o abbassarlo se serve maggiore retrocompatibilità
        //noinspection EditedTargetSdkVersion
        targetSdk = 37 // Aggiornato ad Android 17 (API 37)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // 10.0.2.2 = host machine vista dall'emulatore Android.
            // Il gateway gira su :8080 in dev.
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        release {
            // Abilita la rimozione di codice inutilizzato e l'offuscamento
            isMinifyEnabled = true

            // Abilita la rimozione di risorse inutilizzate (ottimizza le dimensioni)
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Placeholder finche' non avremo un dominio reale.
            buildConfigField("String", "API_BASE_URL", "\"https://api.sanmartino.example.org/\"")
        }
    }
    compileOptions {
        // JDK 21 = LTS, supportato da AGP 9 + Compose + D8.
        // Il Kotlin compiler 2.0.x accetta jvmTarget fino a 24; il backend usa JDK 26
        // ma quella e' una scelta server-side che non si propaga all'app Android.
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// AGP 9 ha rimosso il supporto a `Provider<Directory>` nell'API SourceSet legacy
// (`android.sourceSets.main.java.srcDir(...)`). La via attuale e' la Variant API.
// `addStaticSourceDirectory` accetta un path String risolto eager: ok, perche'
// le cartelle di output sono note al config time. La dipendenza task viene
// mantenuta separatamente via `preBuild.dependsOn(...)` piu' sotto.
androidComponents {
    onVariants { variant ->
        listOf("events", "stands").forEach { name ->
            val generatedDir = layout.buildDirectory
                .dir("generated/openapi/$name/src/main/kotlin")
                .get()
                .asFile
                .absolutePath
            (variant.sources.kotlin ?: variant.sources.java)
                ?.addStaticSourceDirectory(generatedDir)
        }
    }
}

// --- OpenAPI Generator ---------------------------------------------------------------
// Generiamo due client Retrofit (events + stands) a partire dagli spec versionati in
// `app/src/main/openapi/` — single source of truth dal punto di vista del client.
// Per aggiornarli al contratto piu' recente del backend: `./gradlew :app:syncOpenApiSpecs`,
// poi commit del diff.

val backendApiDir: Provider<String> = providers.gradleProperty("backendApiDir")
    .orElse("src/main/openapi")
    .map { File(projectDir, it).absolutePath }

val backendSpecsSource: Provider<String> = providers.gradleProperty("backendSpecsSource")
    .orElse("../../san-martino-services/api")
    .map { File(projectDir, it).absolutePath }

val syncOpenApiSpecs = tasks.register<Copy>("syncOpenApiSpecs") {
    description = "Copia gli OpenAPI specs dal repo backend nella cartella locale del client."
    group = "openapi tools"
    from(backendSpecsSource) {
        include("events-api.yaml", "stands-api.yaml")
    }
    into(backendApiDir)
    doFirst {
        val src = File(backendSpecsSource.get())
        if (!src.exists()) {
            throw GradleException(
                "Backend specs source non trovato: ${src.absolutePath}. " +
                    "Imposta -PbackendSpecsSource=<path> o controlla la struttura del filesystem."
            )
        }
    }
}

fun GenerateTask.commonKotlinClientConfig() {
    generatorName.set("kotlin")
    library.set("jvm-retrofit2")
    configOptions.set(
        mapOf(
            "serializationLibrary" to "kotlinx_serialization",
            "useCoroutines" to "true",
            "dateLibrary" to "java8",
            "modelNameSuffix" to "Dto",
            "enumPropertyNaming" to "UPPERCASE",
            "generateApiTests" to "false",
            "generateModelTests" to "false",
            "generateApiDocumentation" to "false",
            "generateModelDocumentation" to "false",
        )
    )
    skipOverwrite.set(false)
}

val openApiGenerateEvents = tasks.register<GenerateTask>("openApiGenerateEvents") {
    group = "openapi tools"
    description = "Genera il client Kotlin per le API degli eventi."
    commonKotlinClientConfig()

    // CORREZIONE: Usiamo layout.projectDirectory.file()
    inputSpec.set(backendApiDir.map { layout.projectDirectory.file("$it/events-api.yaml") })

    outputDir.set(layout.buildDirectory.dir("generated/openapi/events"))
    apiPackage.set("com.ginogipsy.sanmartinoapp.network.generated.events.api")
    modelPackage.set("com.ginogipsy.sanmartinoapp.network.generated.events.model")
    packageName.set("com.ginogipsy.sanmartinoapp.network.generated.events")
}

val openApiGenerateStands = tasks.register<GenerateTask>("openApiGenerateStands") {
    group = "openapi tools"
    description = "Genera il client Kotlin per le API degli stands."
    commonKotlinClientConfig()

    // CORREZIONE: Usiamo layout.projectDirectory.file()
    inputSpec.set(backendApiDir.map { layout.projectDirectory.file("$it/stands-api.yaml") })

    outputDir.set(layout.buildDirectory.dir("generated/openapi/stands"))
    apiPackage.set("com.ginogipsy.sanmartinoapp.network.generated.stands.api")
    modelPackage.set("com.ginogipsy.sanmartinoapp.network.generated.stands.model")
    packageName.set("com.ginogipsy.sanmartinoapp.network.generated.stands")
}

tasks.named("preBuild") {
    dependsOn(openApiGenerateEvents, openApiGenerateStands)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.scalars)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}