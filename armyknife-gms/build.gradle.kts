apply(from = "../dsl/android-library.gradle")
apply(from = "../dsl/ktlint.gradle")
apply(from = "../dsl/bintray.gradle")

dependencies {
    "api"("com.eaglesakura.armyknife.armyknife-jetpack:armyknife-jetpack:1.4.1")

    "implementation"("androidx.fragment:fragment:1.2.0-rc01")

    /**
     * Google Play Services
     */
    "implementation"("com.google.android.gms:play-services-auth:17.0.0")

    /**
     * Firebase
     */
    "implementation"("com.google.firebase:firebase-core:17.2.1")
    "implementation"("com.google.firebase:firebase-auth:19.1.0")
    "implementation"("com.google.firebase:firebase-config:19.0.3")
    "implementation"("com.google.firebase:firebase-iid:20.0.0")
    "implementation"("com.google.firebase:firebase-firestore:21.2.1")
    "implementation"("com.google.firebase:firebase-storage:19.1.0")
    "implementation"("com.crashlytics.sdk.android:crashlytics:2.10.1")
}