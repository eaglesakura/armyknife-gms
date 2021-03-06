package com.eaglesakura.armyknife.android.gms

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import java.nio.charset.Charset
import org.json.JSONObject

/**
 * Firebase access util.
 */
object Firebase {
    /**
     * for Unit Test.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun provideFromAssets(context: Context, googleServicesJsonPath: String) =
        provideFromGoogleServiceJson(
            context,
            context.assets.open(googleServicesJsonPath).use {
                it.readBytes().toString(Charset.forName("UTF-8"))
            }
        )

    /**
     * for UnitTest.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun provideFromGoogleServiceJson(context: Context, json: String) {
        firebaseAppProvider = fun(name: String): FirebaseApp {
            val options = FirebaseOptions.Builder().also { builder ->
                val root = JSONObject(json)
                root.getJSONObject("project_info").also { values ->
                    builder.setDatabaseUrl(values["firebase_url"].toString())
                    builder.setGcmSenderId(values["project_number"].toString())
                    builder.setProjectId(values["project_id"].toString())
                    builder.setStorageBucket(values["storage_bucket"].toString())
                }
                root.getJSONArray("client").getJSONObject(0).also { client ->
                    client.getJSONObject("client_info").also { values ->
                        builder.setApplicationId(values["mobilesdk_app_id"].toString())
                    }
                    client.getJSONArray("api_key").getJSONObject(0).also { apiKey ->
                        builder.setApiKey(apiKey["current_key"].toString())
                    }
                }
            }.build()

            return if (FirebaseApp.getApps(context).find { it.name == name } == null) {
                FirebaseApp.initializeApp(context, options, name)
            } else {
                FirebaseApp.getInstance(name)
            }
        }
    }

    /**
     * FirebaseApp instance factory.
     */
    var firebaseAppProvider = fun(name: String): FirebaseApp {
        return if (name.isEmpty()) {
            FirebaseApp.getInstance()
        } else {
            FirebaseApp.getInstance(name)
        }
    }

    /**
     * get FirebaseApp instance by name.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun app(name: String): FirebaseApp? {
        return try {
            return firebaseAppProvider(name)
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Firebase app default instance.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val app: FirebaseApp?
        get() = app(FirebaseApp.DEFAULT_APP_NAME)

    /**
     * Linked module(firebase-core)
     */
    val linkAppModule: Boolean by lazy {
        try {
            Log.d("Firebase", "${FirebaseApp::class.java.simpleName} is linking")
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * get FirebaseInstanceId by name.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun installations(name: String): FirebaseInstallations? {
        return try {
            FirebaseInstallations.getInstance(app(name)!!)
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Linked module(firebase-iid)
     */
    val linkInstallationsModule: Boolean by lazy {
        try {
            Log.d("Firebase", "${FirebaseInstallations::class.java.simpleName} is linking")
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * default Firebase Cloud Firestore instance.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun firestore(name: String): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance(app(name)!!)
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * default Firebase Cloud Firestore instance.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance(app!!)
        } catch (e: Throwable) {
            null
        }

    /**
     * Linked module(firestore)
     */
    val linkFirestoreModule: Boolean by lazy {
        try {
            Log.d("Firebase", "${FirebaseFirestore::class.java.simpleName} is linking")
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Firebase Remote Config instance by name.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun remoteConfig(name: String): FirebaseRemoteConfig? {
        return try {
            FirebaseRemoteConfig.getInstance(app(name)!!)
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * default Firebase Remote Config instance.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val remoteConfig: FirebaseRemoteConfig?
        get() = try {
            app!!
            FirebaseRemoteConfig.getInstance()
        } catch (e: Throwable) {
            null
        }

    /**
     * Linked module(firebase-config)
     */
    val linkRemoteConfigModule: Boolean by lazy {
        try {
            Log.d("Firebase", "${FirebaseRemoteConfig::class.java.simpleName} is linking")
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Firebase auth instance by name.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun auth(name: String): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance(app(name)!!)
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * default Firebase auth instance.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance(app!!)
        } catch (e: Throwable) {
            null
        }

    /**
     * Linked module(firebase-auth)
     */
    val linkAuthModule: Boolean by lazy {
        try {
            Log.d("Firebase", "${FirebaseAuth::class.java.simpleName} is linking")
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Firebase analytics instance.
     *
     * Required Permissions)
     *  1. android.permission.ACCESS_NETWORK_STATE
     *  2. android.permission.INTERNET
     *  3. android.permission.WAKE_LOCK
     */
    @SuppressLint("MissingPermission")
    fun analytics(context: Context): FirebaseAnalytics? = try {
        FirebaseAnalytics.getInstance(context)
    } catch (e: Throwable) {
        null
    }

    /**
     * Linked module(firebase-core)
     */
    val linkAnalyticsModule: Boolean by lazy {
        try {
            Log.d("Firebase", "${FirebaseAnalytics::class.java.simpleName} is linking")
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * returns Firebase storage instance.
     */
    fun storage(url: String = "", name: String): FirebaseStorage? {
        // make a storage.
        return try {
            val app = app(name)!!
            if (url.isEmpty()) {
                FirebaseStorage.getInstance(app)
            } else {
                FirebaseStorage.getInstance(app, url)
            }
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Linked module(firebase-storage)
     */
    val linkStorageModule: Boolean by lazy {
        try {
            Log.d("Firebase", "${FirebaseStorage::class.java.simpleName} is linking")
            true
        } catch (e: Throwable) {
            false
        }
    }
}
