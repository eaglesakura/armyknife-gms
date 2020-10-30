package com.eaglesakura.armyknife.android.gms

import android.content.Context
import android.util.Log
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import com.eaglesakura.armyknife.android.extensions.UIHandler
import com.eaglesakura.armyknife.android.extensions.assertUIThread
import com.eaglesakura.armyknife.android.extensions.awaitInCoroutines
import com.eaglesakura.armyknife.android.extensions.debugMode
import com.eaglesakura.armyknife.android.extensions.runBlockingOnUiThread
import com.eaglesakura.armyknife.android.gms.GooglePlayService.coroutineScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Firebase current context LiveData.
 *
 * Update on
 *  - User sign-in/sign-out
 *  - User auth token refresh
 *  - Instance id refresh
 *  - Sync remote configs
 *
 *  e.g.
 *  val firebaseContext = FirebaseContext.getInstance(context)
 *  firebaseContext.observeForever {
 *      // on update firebase status.
 *  }
 */
class FirebaseContext internal constructor(
    private val context: Context,
    private val name: String,
    @Suppress("MemberVisibilityCanBePrivate") val app: FirebaseApp?,
    @Suppress("MemberVisibilityCanBePrivate") val installations: FirebaseInstallations?,
    @Deprecated("FirebaseInstanceId is deprecated") @Suppress("MemberVisibilityCanBePrivate") val instanceId: FirebaseInstanceId?,
    @Suppress("MemberVisibilityCanBePrivate") val auth: FirebaseAuth?,
    @Suppress("MemberVisibilityCanBePrivate") val remoteConfig: FirebaseRemoteConfig?
) : LiveData<FirebaseContextSnapshot>(), Closeable {

    /**
     * This context is default Firebase instance.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val isDefault: Boolean
        get() = (name == FirebaseApp.DEFAULT_APP_NAME)

    private val tag = "FirebaseContext($name)"

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate + Job())

    /**
     * Refresh FirebaseAuth token interval(milli seconds)
     */
    private val userAuthTokenRefreshInterval: Long = when (context.debugMode) {
        true -> TimeUnit.MINUTES.toMillis(5)
        else -> TimeUnit.MINUTES.toMillis(30)
    }

    /**
     * Refresh FirebaseAuth token interval(milli seconds)
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var remoteConfigRefreshInterval: Long = when (context.debugMode) {
        true -> TimeUnit.MINUTES.toMillis(1)
        else -> TimeUnit.MINUTES.toMillis(55)
    }
        set(value) {
            check(value > 0)
            field = value
        }

    private var firebaseUser: FirebaseUser? = null

    private var instanceIdResult: InstanceIdResult? = null

    /**
     * FirebaseInstallation id.
     */
    private var installationId: String? = null

    private var authTokenResult: GetTokenResult? = null

    private var authRefreshJob: Job? = null

    init {
        if (app != null) {
            app.addLifecycleEventListener { _, _ ->
                Log.d(tag, "onDestroy($name)")
                synchronized(instances) {
                    instances.remove(name)
                }
            }
        } else {
            if (Firebase.linkAppModule) {
                Log.d(tag, "installed GMS(com.google.firebase:firebase-core)")
            } else {
                Log.d(tag, "no-dependencies(com.google.firebase:firebase-core)")
            }
        }
        if (auth != null) {
            refreshAuth()
            auth.addAuthStateListener(FirebaseAuth.AuthStateListener { refreshAuth() })
        } else {
            if (Firebase.linkAuthModule) {
                Log.d(tag, "installed GMS(com.google.firebase:firebase-auth)")
            } else {
                Log.d(tag, "no-dependencies(com.google.firebase:firebase-auth)")
            }
        }
        if (remoteConfig != null) {
            startConfigRefreshLoop()
        } else {
            if (Firebase.linkRemoteConfigModule) {
                Log.d(tag, "installed GMS(com.google.firebase:firebase-config)")
            } else {
                Log.d(tag, "no-dependencies(com.google.firebase:firebase-config)")
            }
        }
        if (instanceId != null) {
            refreshInstanceId(instanceId)
        } else {
            if (Firebase.linkInstanceIdModule) {
                Log.d(tag, "installed GMS(com.google.firebase:firebase-iid)")
            } else {
                Log.d(tag, "no-dependencies(com.google.firebase:firebase-iid)")
            }
        }
        if (installations != null) {
            refreshInstallation(installations)
        } else {
            if (Firebase.linkInstallationsModule) {
                Log.d(tag, "installed GMS(com.google.firebase:firebase-installations)")
            } else {
                Log.d(tag, "no-dependencies(com.google.firebase:firebase-installations)")
            }
        }
        snapshot()
    }

    /**
     * Refresh ContextSnapshot async.
     */
    @AnyThread
    fun refresh() {
        UIHandler.post {
            snapshot()
        }
    }

    /**
     * Close instance.
     */
    override fun close() {
        synchronized(instances) {
            coroutineScope.cancel()
            instances.remove(name)
        }
    }

    @UiThread
    private fun refreshAuth() {
        assertUIThread()
        val auth = this.auth ?: return

        val oldUser = this.firebaseUser
        val newUser = auth.currentUser

        // not login now.
        if (newUser == null) {
            this.authRefreshJob?.cancel()
            this.authRefreshJob = null
            this.authTokenResult = null
        }
        this.firebaseUser = newUser

        when {
            oldUser == null && newUser != null -> {
                // user login
                Log.i(tag, "User login")
                startAuthTokenRefreshLoop(auth, newUser)
            }
            oldUser != null && newUser == null -> {
                // user logout
                Log.i(tag, "User logout")
            }
        }
        snapshot()
    }

    @UiThread
    private fun refreshInstallation(firebaseInstallations: FirebaseInstallations) {
        coroutineScope.launch {
            while (isActive) {
                try {
                    val installId = firebaseInstallations.id.awaitInCoroutines()
                    if (!installId.isSuccessful) {
                        throw installId.exception!!
                    }
                    withContext(Dispatchers.Main) {
                        if (installId.isSuccessful) {
                            this@FirebaseContext.installationId = installId.result!!
                            snapshot()
                        }
                    }
                    return@launch
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @UiThread
    private fun refreshInstanceId(firebaseInstanceId: FirebaseInstanceId) {
        coroutineScope.launch {
            while (isActive) {
                try {
                    val instanceId = firebaseInstanceId.instanceId.awaitInCoroutines()
                    if (!instanceId.isSuccessful) {
                        throw instanceId.exception!!
                    }
                    withContext(Dispatchers.Main) {
                        if (instanceId.isSuccessful) {
                            this@FirebaseContext.instanceIdResult = instanceId.result!!
                            snapshot()
                        }
                    }

                    return@launch
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @UiThread
    private fun snapshot() {
        assertUIThread()

        val snapshot = FirebaseContextSnapshot(
            user = firebaseUser,
            instanceId = instanceIdResult,
            installationsId = installationId,
            userAuthToken = authTokenResult,
            remoteConfigValues = try {
                Firebase.remoteConfig?.all?.toMap() ?: emptyMap()
            } catch (e: Throwable) {
                @Suppress("RemoveExplicitTypeArguments" /* for Intellij compiler */)
                emptyMap<String, FirebaseRemoteConfigValue>()
            },
            remoteConfigFetchStatus = try {
                Firebase.remoteConfig?.info?.lastFetchStatus
                    ?: FirebaseRemoteConfig.LAST_FETCH_STATUS_NO_FETCH_YET
            } catch (e: Throwable) {
                FirebaseRemoteConfig.LAST_FETCH_STATUS_NO_FETCH_YET
            }
        )
        Log.d(tag, "refresh FirebaseContextSnapshot/${snapshot.id}/${snapshot.date}")
        this.value = snapshot
    }

    private fun startConfigRefreshLoop() {
        val remoteConfig = this.remoteConfig ?: return

        coroutineScope.launch {
            while (isActive) {
                try {
                    Log.d(tag, "RemoteConfig.fetch")
                    remoteConfig.fetch(remoteConfigRefreshInterval).awaitInCoroutines()
                        .also { task ->
                            task.exception?.also { e ->
                                Log.i(tag, "fetch failed")
                                throw e
                            }
                        }
                    withContext(Dispatchers.Main) {
                        Log.i(tag, "RemoteConfig.snapshot")
                        snapshot()
                    }
                    delay(remoteConfigRefreshInterval) // refresh next 60 min later.
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun startAuthTokenRefreshLoop(
        @Suppress("UNUSED_PARAMETER") auth: FirebaseAuth,
        userSnapshot: FirebaseUser
    ) {
        authRefreshJob?.cancel()
        authRefreshJob = coroutineScope.launch {
            while (userSnapshot == this@FirebaseContext.firebaseUser) {
                try {
                    Log.i(tag, "sync user token")
                    val task = userSnapshot.getIdToken(true).awaitInCoroutines()
                    withContext(Dispatchers.Main) {
                        if (task.isSuccessful && userSnapshot == this@FirebaseContext.firebaseUser) {
                            Log.i(tag, "refresh user token")
                            this@FirebaseContext.authTokenResult = task.result
                            snapshot()
                        } else if (!task.isSuccessful) {
                            throw task.exception!!
                        }
                    }
                    delay(userAuthTokenRefreshInterval)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(TimeUnit.SECONDS.toMillis(1))
                }
            }
        }
    }

    override fun toString(): String {
        return "FirebaseContext(name='${
        if (isDefault) {
            "[DEFAULT]"
        } else {
            name
        }
        }')"
    }

    companion object {
        @VisibleForTesting
        internal val instances = mutableMapOf<String, FirebaseContext>()

        /**
         * Get Firebase snapshots.
         */
        fun getInstance(
            context: Context,
            name: String = FirebaseApp.DEFAULT_APP_NAME,
        ): FirebaseContext {
            return runBlockingOnUiThread {
                synchronized(instances) {
                    instances[name]?.also {
                        return@runBlockingOnUiThread it
                    }

                    val firebaseContext = FirebaseContext(
                        context = context,
                        name = name,
                        app = Firebase.app(name),
                        auth = Firebase.auth(name),
                        instanceId = Firebase.instanceId(name),
                        installations = Firebase.installations(name),
                        remoteConfig = Firebase.remoteConfig(name)
                    )
                    instances[name] = firebaseContext
                    firebaseContext
                }
            }
        }
    }
}
