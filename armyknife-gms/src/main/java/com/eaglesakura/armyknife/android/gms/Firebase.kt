package com.eaglesakura.armyknife.android.gms

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Firebase access util.
 */
object Firebase {

    /**
     * for Unit Test.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun provideFromAssets(context: Context, googleServicesJsonPath: String) =
            provideFromGoogleServiceJson(context, context.assets.open(googleServicesJsonPath).use {
                it.readBytes().toString(Charset.forName("UTF-8"))
            })

    /**
     * for UnitTest.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun provideFromGoogleServiceJson(context: Context, json: String) {
        firebaseAppProvider = {
            FirebaseApp.initializeApp(context, FirebaseOptions.Builder().also { builder ->
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
            }.build())
        }
    }

    /**
     * FirebaseApp instance factory.
     */
    var firebaseAppProvider: (() -> FirebaseApp) = {
        FirebaseApp.getInstance()
    }

    /**
     * Firebase app instance.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val app: FirebaseApp? by lazy {
        try {
            firebaseAppProvider()
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Linked module(firebase-core)
     */
    val linkAppModule: Boolean by lazy {
        try {
            app
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Firebase instance ID
     */
    val instanceId: FirebaseInstanceId? by lazy {
        try {
            FirebaseInstanceId.getInstance(app!!)
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Linked module(firebase-iid)
     */
    val linkInstanceIdModule: Boolean by lazy {
        try {
            instanceId
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Firebase Cloud Firestore instance.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance(app!!)
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Linked module(firestore)
     */
    val linkFirestoreModule: Boolean by lazy {
        try {
            firestore
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Firebase Remote Config instance.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val remoteConfig: FirebaseRemoteConfig? by lazy {
        try {
            app!!
            FirebaseRemoteConfig.getInstance()
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Linked module(firebase-config)
     */
    val linkRemoteConfigModule: Boolean by lazy {
        try {
            remoteConfig
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Firebase auth instance.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance(app!!)
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Linked module(firebase-auth)
     */
    val linkAuthModule: Boolean by lazy {
        try {
            auth
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Firebase analytics isinstance.
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
    fun linkAnalyticsModule(context: Context): Boolean {
        return try {
            FirebaseAnalytics.getInstance(context)
            true
        } catch (e: Throwable) {
            false
        }
    }

    private val lock = ReentrantLock()

    private val storageCaches = mutableMapOf<String, FirebaseStorage>()

    /**
     * returns Firebase storage instance.
     */
    fun storage(url: String = ""): FirebaseStorage? {
        lock.withLock {
            if (storageCaches[url] != null) {
                return storageCaches[url]
            }

            // make a storage.
            return try {
                storageCaches[url] = if (url.isEmpty()) {
                    FirebaseStorage.getInstance(app!!)
                } else {
                    FirebaseStorage.getInstance(app!!, url)
                }
                storageCaches[url]
            } catch (e: Throwable) {
                null
            }
        }
    }

    /**
     * Linked module(firebase-storage)
     */
    val linkStorageModule: Boolean by lazy {
        try {
            storage()
            true
        } catch (e: Throwable) {
            false
        }
    }
}