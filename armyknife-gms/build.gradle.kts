apply(from = "../dsl/android-library.gradle")
apply(from = "../dsl/ktlint.gradle")
apply(from = "../dsl/bintray.gradle")

dependencies {
    "api"("com.eaglesakura.armyknife.armyknife-jetpack:armyknife-jetpack:1.4.9")

    "implementation"("androidx.fragment:fragment:1.3.0-alpha01")

    /**
     * Google Play Services
     */
    "compileOnly"("com.google.android.gms:play-services-base:17.1.0")
    "compileOnly"("com.google.android.gms:play-services-auth:17.0.0")

    /**
     * Firebase
     */
    "compileOnly"("com.google.firebase:firebase-core:17.2.3")
    "compileOnly"("com.google.firebase:firebase-auth:19.2.0")
    "compileOnly"("com.google.firebase:firebase-config:19.1.2")
    "compileOnly"("com.google.firebase:firebase-iid:20.1.1")
    "compileOnly"("com.google.firebase:firebase-firestore:21.4.1")
    "compileOnly"("com.google.firebase:firebase-storage:19.1.1")
    "compileOnly"("com.crashlytics.sdk.android:crashlytics:2.10.1")
}