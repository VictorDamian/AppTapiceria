plugins {
    // Plugin de la aplicación Android.
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Soporte para Jetpack Compose.
    alias(libs.plugins.kotlin.compose)

    // Procesador de código para generar la implementación de Room.
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.tapiceria.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.tapiceria.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    // 1. Compatibilidad nativa de Java cambiada a 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Base de datos local SQLite mediante Room.
    val roomVersion = "2.7.2"

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // KSP genera el código requerido por Room.
    ksp("androidx.room:room-compiler:$roomVersion")

    // Corrutinas para operaciones asíncronas.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Pruebas unitarias.
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    // ViewModel integrado con Jetpack Compose.
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")

    // Permite que Compose observe StateFlow desde el ciclo de vida.
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")

    // Carga y muestra imágenes en Jetpack Compose.
    implementation("io.coil-kt:coil-compose:2.7.0")
}

    // Configura la ubicación de los esquemas de Room para futuras migraciones.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}