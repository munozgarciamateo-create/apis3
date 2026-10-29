import java.util.Properties

// Datos de SQLite Cloud: se leen de local.properties (no se sube a GitHub)
val propiedadesLocales = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
fun propiedad(nombre: String): String = propiedadesLocales.getProperty(nombre, "")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.3.6"
    // Firebase: lee app/google-services.json
    id("com.google.gms.google-services")
}

// Datos de la llave de firma. Viven en keystore.properties (fuera de git), nunca en este archivo.
val keystoreProperties = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

android {
    namespace = "com.example.apis3"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Identificador en Google Play: único y NO se puede cambiar después de publicar.
        // (namespace sigue siendo com.example.apis3: es solo el paquete de las clases Kotlin)
        applicationId = "com.mateomunoz.apis3"
        minSdk = 24
        targetSdk = 37          // Play exige 36 o más desde el 31/08/2026
        versionCode = 1         // entero: sube en CADA subida a Play (1, 2, 3...)
        versionName = "1.0"     // lo que ven los usuarios

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SQLITECLOUD_FUNCTIONS_URL", "\"${propiedad("sqlitecloud.functionsUrl")}\"")
        buildConfigField("String", "SQLITECLOUD_API_KEY", "\"${propiedad("sqlitecloud.apiKey")}\"")
    }

    signingConfigs {
        create("release") {
            // Si keystore.properties no existe (por ejemplo en la máquina de un compañero), no se firma
            if (keystoreProperties.isNotEmpty()) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // R8 (DSL nuevo de AGP 9.3+): reduce y ofusca el código y quita recursos sin usar.
            // Equivale a isMinifyEnabled + isShrinkResources + proguard-android-optimize.txt.
            // Las reglas propias van en src/main/keepRules/rules.keep
            optimization {
                enable = true
            }
            if (keystoreProperties.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
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

    // Retrofit, OkHttp, Coil y ViewModel
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.material:material-icons-core")

    // Room (con KSP, porque kapt no funciona con AGP 9)
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    // Navegación entre pantallas (paso 8.5)
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Firebase Authentication. El BoM fija versiones compatibles entre sí (por eso firebase-auth no lleva versión)
    // 34.12.0 y no 35.x: Firebase 35 está compilado con Kotlin 2.4 y este proyecto usa Kotlin 2.2
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-auth")
    // Permite usar .await() sobre las tareas (Task) de Firebase dentro de corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}