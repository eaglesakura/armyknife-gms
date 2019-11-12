package com.eaglesakura.armyknife.android.gms

import android.content.Context
import android.util.Log
import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import com.eaglesakura.armyknife.android.extensions.assertUIThread
import com.eaglesakura.armyknife.android.extensions.awaitInCoroutines
import com.eaglesakura.armyknife.android.extensions.debugMode
import com.eaglesakura.armyknife.runtime.LazySingleton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

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
class FirebaseContext internal constructor(val context: Context) :
    LiveData<FirebaseContextSnapshot>() {

    private val tag = "FirebaseContext"

    private val userAuthTokenRefreshInterval: Long = when (context.debugMode) {
        true -> TimeUnit.MINUTES.toMillis(5)
        else -> TimeUnit.MINUTES.toMillis(30)
    }

    private val remoteConfigRefreshInterval: Long = when (context.debugMode) {
        true -> TimeUnit.MINUTES.toMillis(10)
        else -> TimeUnit.MINUTES.toMillis(60)
    }

    @Suppress("ObjectLiteralToLambda")
    private val listener = object : FirebaseAuth.AuthStateListener {
        override fun onAuthStateChanged(auth: FirebaseAuth) {
            refreshAuth(auth)
        }
    }

    private var user: FirebaseUser? = null

    private var instanceId: InstanceIdResult? = null

    private var authToken: GetTokenResult? = null

    private var authRefreshJob: Job? = null

    init {
        Firebase.auth?.also { auth ->
            refreshAuth(auth)
            auth.addAuthStateListener(listener)
        }
        Firebase.remoteConfig?.also { config ->
            startConfigRefreshLoop(config)
        }
        Firebase.instanceId?.also { firebaseInstanceId ->
            refreshInstanceId(firebaseInstanceId)
        }
        snapshot()
    }

    @UiThread
    private fun refreshAuth(auth: FirebaseAuth) {
        assertUIThread()
        val oldUser = this.user
        val newUser = auth.currentUser

        // not login now.
        if (newUser == null) {
            this.authRefreshJob?.cancel()
            this.authRefreshJob = null
            this.authToken = null
        }
        this.user = newUser

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
    private fun refreshInstanceId(firebaseInstanceId: FirebaseInstanceId) {
        GooglePlayService.coroutineScope.launch {
            while (isActive) {
                try {
                    val instanceId = firebaseInstanceId.instanceId.awaitInCoroutines()
                    if (!instanceId.isSuccessful) {
                        throw instanceId.exception!!
                    }
                    withContext(Dispatchers.Main) {
                        if (instanceId.isSuccessful) {
                            this@FirebaseContext.instanceId = instanceId.result!!
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

        Firebase.remoteConfig?.info?.lastFetchStatus
        val snapshot = FirebaseContextSnapshot(
            user = user,
            instanceId = instanceId,
            userAuthToken = authToken,
            remoteConfigValues = Firebase.remoteConfig?.all?.toMap() ?: emptyMap(),
            remoteConfigFetchStatus = Firebase.remoteConfig?.info?.lastFetchStatus
                ?: FirebaseRemoteConfig.LAST_FETCH_STATUS_NO_FETCH_YET
        )
        Log.i(tag, "send FirebaseContextSnapshot/${snapshot.id}/${snapshot.date}")
        this.value = snapshot
    }

    private fun startConfigRefreshLoop(config: FirebaseRemoteConfig) {
        GooglePlayService.coroutineScope.launch {
            while (isActive) {
                try {
                    Log.i(tag, "RemoteConfig.fetch")
                    config.fetch(remoteConfigRefreshInterval).awaitInCoroutines().also { task ->
                        task.exception?.also { e ->
                            Log.i(tag, "fetch failed")
                            throw e
                        }
                    }
                    Log.i(
                        tag,
                        "RemoteConfig.activate / lastFetchStatus='${config.info.lastFetchStatus}'"
                    )
                    config.activate().awaitInCoroutines().also { task ->
                        task.exception?.also { e ->
                            Log.i(tag, "activate failed")
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

    private fun startAuthTokenRefreshLoop(auth: FirebaseAuth, userSnapshot: FirebaseUser) {
        authRefreshJob?.cancel()
        authRefreshJob = GooglePlayService.coroutineScope.launch {
            while (userSnapshot == this@FirebaseContext.user) {
                try {
                    Log.i(tag, "sync user token")
                    val task = userSnapshot.getIdToken(true).awaitInCoroutines()
                    withContext(Dispatchers.Main) {
                        if (task.isSuccessful && userSnapshot == this@FirebaseContext.user) {
                            Log.i(tag, "refresh user token")
                            this@FirebaseContext.authToken = task.result
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
                }
            }
        }
    }

    companion object {
        private val instanceImpl = LazySingleton<FirebaseContext>()

        /**
         * new instance.
         */
        fun getInstance(context: Context): FirebaseContext {
            return instanceImpl.get {
                FirebaseContext(context = context.applicationContext)
            }
        }
    }
}