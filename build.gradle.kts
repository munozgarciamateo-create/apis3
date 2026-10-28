// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Firebase: procesa google-services.json y lo convierte en recursos de la app
    id("com.google.gms.google-services") version "4.5.0" apply false
}
// iCloud no sincroniza carpetas que terminan en ".nosync".
// Así evita crear copias duplicadas ("Archivo 3.class") que rompen la compilación.
allprojects {
    layout.buildDirectory.set(file("build.nosync"))
}
