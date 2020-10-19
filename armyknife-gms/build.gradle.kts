apply(from = "../dsl/android-library.gradle")
apply(from = "../dsl/ktlint.gradle")
apply(from = "../dsl/bintray.gradle")
apply(from = "../dsl/line-separator.gradle.kts")

dependencies {
    "api"("com.eaglesakura.armyknife.armyknife-jetpack:armyknife-jetpack:1.4.11")

    "compileOnly"("androidx.annotation:annotation:1.1.0")
    "compileOnly"("androidx.core:core:1.3.2")
    "compileOnly"("androidx.core:core-ktx:1.3.2")
    "compileOnly"("androidx.appcompat:appcompat:1.2.0")
    "compileOnly"("androidx.appcompat:appcompat-resources:1.2.0")
    "compileOnly"("androidx.lifecycle:lifecycle-extensions:2.2.0")
    "compileOnly"("androidx.lifecycle:lifecycle-runtime:2.2.0")
    "compileOnly"("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    "compileOnly"("androidx.lifecycle:lifecycle-common-java8:2.2.0")

    /**
     * Google Play Services
     */
    "compileOnly"("com.google.android.gms:play-services-base:17.4.0")
    "compileOnly"("com.google.android.gms:play-services-auth:18.1.0")

    /**
     * Firebase
     */
    "compileOnly"("com.google.firebase:firebase-core:17.5.1")
    "compileOnly"("com.google.firebase:firebase-auth:19.4.0")
    "compileOnly"("com.google.firebase:firebase-config:19.2.0")
    "compileOnly"("com.google.firebase:firebase-iid:20.3.0")
    "compileOnly"("com.google.firebase:firebase-firestore:21.7.1")
    "compileOnly"("com.google.firebase:firebase-storage:19.2.0")
    "compileOnly"("com.google.firebase:firebase-storage:19.2.0")
    "compileOnly"("com.google.firebase:firebase-crashlytics:17.2.2")

    "testImplementation"("com.google.firebase:firebase-core:17.5.1")
    "testImplementation"("com.google.firebase:firebase-auth:19.4.0")
    "testImplementation"("com.google.firebase:firebase-config:19.2.0")
    "testImplementation"("com.google.firebase:firebase-iid:20.3.0")
    "testImplementation"("com.google.firebase:firebase-firestore:21.7.1")
    "testImplementation"("com.google.firebase:firebase-storage:19.2.0")

    "androidTestImplementation"("com.google.firebase:firebase-core:17.5.1")
    "androidTestImplementation"("com.google.firebase:firebase-auth:19.4.0")
    "androidTestImplementation"("com.google.firebase:firebase-config:19.2.0")
    "androidTestImplementation"("com.google.firebase:firebase-iid:20.3.0")
    "androidTestImplementation"("com.google.firebase:firebase-firestore:21.7.1")
    "androidTestImplementation"("com.google.firebase:firebase-storage:19.2.0")
}