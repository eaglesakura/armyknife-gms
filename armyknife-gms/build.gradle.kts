apply(from = "../dsl/android-library.gradle")
apply(from = "../dsl/ktlint.gradle")
apply(from = "../dsl/bintray.gradle")

dependencies {
    "api"("com.eaglesakura.armyknife.armyknife-jetpack:armyknife-jetpack:1.3.0")

    /**
     * Google Play Services
     */
    "implementation"("com.google.android.gms:play-services-auth:16.0.1")

    /**
     * Firebase
     */
    "implementation"("com.google.firebase:firebase-core:16.0.9")
    "implementation"("com.google.firebase:firebase-auth:17.0.0")
    "implementation"("com.google.firebase:firebase-config:16.5.0")
    "implementation"("com.google.firebase:firebase-iid:17.1.2")
    "implementation"("com.google.firebase:firebase-firestore:19.0.0")
    "implementation"("com.google.firebase:firebase-storage:17.0.0")
    "implementation"("com.crashlytics.sdk.android:crashlytics:2.10.0")
}