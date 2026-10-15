
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.rivo.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rivo.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 9
        versionName = "9.0"
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))

    // Firebase телефон ва SMS
    implementation("com.google.firebase:firebase-auth")

    // Firebase база барои чат ва паёмҳо
    implementation("com.google.firebase:firebase-firestore")
}
