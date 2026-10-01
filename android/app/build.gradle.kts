plugins {
    id("com.android.application")
}
android {
    namespace="com.nexora.durakarena"
    compileSdk=35
    defaultConfig {
        applicationId="com.nexora.durakarena.test01"
        minSdk=23
        targetSdk=35
        versionCode=3
        versionName="0.1"
    }
}
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
}