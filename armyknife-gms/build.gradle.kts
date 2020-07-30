apply(from = "../dsl/android-library.gradle")
apply(from = "../dsl/ktlint.gradle")
apply(from = "../dsl/bintray.gradle")

dependencies {
    "api"("com.eaglesakura.armyknife.armyknife-jetpack:armyknife-jetpack:1.4.9")

    "compileOnly"("androidx.annotation:annotation:1.1.0")
    "compileOnly"("androidx.core:core:1.3.1")
    "compileOnly"("androidx.core:core-ktx:1.3.1")
    "compileOnly"("androidx.appcompat:appcompat:1.1.0")
    "compileOnly"("androidx.appcompat:appcompat-resources:1.1.0")
    "compileOnly"("androidx.lifecycle:lifecycle-extensions:2.2.0")
    "compileOnly"("androidx.lifecycle:lifecycle-runtime:2.2.0")
    "compileOnly"("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    "compileOnly"("androidx.lifecycle:lifecycle-common-java8:2.2.0")

    /**
     * Google Play Services
     */
    "compileOnly"("com.google.android.gms:play-services-base:17.3.0")
    "compileOnly"("com.google.android.gms:play-services-auth:18.1.0")

    /**
     * Firebase
     */
    "compileOnly"("com.google.firebase:firebase-core:17.4.4")
    "compileOnly"("com.google.firebase:firebase-auth:19.3.2")
    "compileOnly"("com.google.firebase:firebase-config:19.2.0")
    "compileOnly"("com.google.firebase:firebase-iid:20.2.3")
    "compileOnly"("com.google.firebase:firebase-firestore:21.5.0")
    "compileOnly"("com.google.firebase:firebase-storage:19.1.1")
    "compileOnly"("com.crashlytics.sdk.android:crashlytics:2.10.1")

    "testImplementation"("com.google.firebase:firebase-core:17.4.4")
    "testImplementation"("com.google.firebase:firebase-auth:19.3.2")
    "testImplementation"("com.google.firebase:firebase-config:19.2.0")
    "testImplementation"("com.google.firebase:firebase-iid:20.2.3")
    "testImplementation"("com.google.firebase:firebase-firestore:21.5.0")
    "testImplementation"("com.google.firebase:firebase-storage:19.1.1")

    "androidTestImplementation"("com.google.firebase:firebase-core:17.4.4")
    "androidTestImplementation"("com.google.firebase:firebase-auth:19.3.2")
    "androidTestImplementation"("com.google.firebase:firebase-config:19.2.0")
    "androidTestImplementation"("com.google.firebase:firebase-iid:20.2.3")
    "androidTestImplementation"("com.google.firebase:firebase-firestore:21.5.0")
    "androidTestImplementation"("com.google.firebase:firebase-storage:19.1.1")
}