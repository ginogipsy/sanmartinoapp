import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.openapi.generator")
}

android {
    namespace = "com.ginogipsy.sanmartinoapp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.ginogipsy.sanmartinoapp"
        minSdk = 36
        targetSdk = 36
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
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Placeholder finche' non avremo un dominio reale.
            buildConfigField("String", "API_BASE_URL", "\"https://api.sanmartino.example.org/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_26
        targetCompatibility = JavaVersion.VERSION_26
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    sourceSets {
        getByName("main").java.srcDirs(
            layout.buildDirectory.dir("generated/openapi/events/src/main/kotlin"),
            layout.buildDirectory.dir("generated/openapi/stands/src/main/kotlin"),
        )
    }
}

// --- OpenAPI Generator ---------------------------------------------------------------
// Generiamo due client Retrofit (events + stands) a partire dagli spec versionati in
// `app/src/main/openapi/` — single source of truth dal punto di vista del client.
// Per aggiornarli al contratto piu' recente del backend: `./gradlew :app:syncOpenApiSpecs`,
// poi commit del diff.

val backendApiDir = providers.gradleProperty("backendApiDir")
    .orElse("src/main/openapi")
    .map { java.io.File(projectDir, it).absolutePath }

val backendSpecsSource = providers.gradleProperty("backendSpecsSource")
    .orElse("../../san-martino-services/api")
    .map { java.io.File(projectDir, it).absolutePath }

val syncOpenApiSpecs = tasks.register<Copy>("syncOpenApiSpecs") {
    description = "Copia gli OpenAPI specs dal repo backend nella cartella locale del client."
    group = "openapi tools"
    from(backendSpecsSource) {
        include("events-api.yaml", "stands-api.yaml")
    }
    into(backendApiDir)
    doFirst {
        val src = java.io.File(backendSpecsSource.get())
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
    commonKotlinClientConfig()
    inputSpec.set(backendApiDir.map { "$it/events-api.yaml" })
    outputDir.set(
        layout.buildDirectory.dir("generated/openapi/events").map { it.asFile.absolutePath }
    )
    apiPackage.set("com.ginogipsy.sanmartinoapp.network.generated.events.api")
    modelPackage.set("com.ginogipsy.sanmartinoapp.network.generated.events.model")
    packageName.set("com.ginogipsy.sanmartinoapp.network.generated.events")
}

val openApiGenerateStands = tasks.register<GenerateTask>("openApiGenerateStands") {
    commonKotlinClientConfig()
    inputSpec.set(backendApiDir.map { "$it/stands-api.yaml" })
    outputDir.set(
        layout.buildDirectory.dir("generated/openapi/stands").map { it.asFile.absolutePath }
    )
    apiPackage.set("com.ginogipsy.sanmartinoapp.network.generated.stands.api")
    modelPackage.set("com.ginogipsy.sanmartinoapp.network.generated.stands.model")
    packageName.set("com.ginogipsy.sanmartinoapp.network.generated.stands")
}

tasks.named("preBuild") {
    dependsOn(openApiGenerateEvents, openApiGenerateStands)
}

dependencies {
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // --- Networking ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
