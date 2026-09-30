plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}
android {
    namespace="com.nexora.durakarena"
    compileSdk=35
    defaultConfig {
        applicationId="com.nexora.durakarena"
        minSdk=23
        targetSdk=35
        versionCode=1
        versionName="0.1.0"
    }
}
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
}