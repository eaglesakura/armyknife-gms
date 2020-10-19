package com.eaglesakura.armyknife.android.gms.extensions

import android.os.Bundle
import com.eaglesakura.armyknife.android.gms.error.DeveloperImplementFailedException
import com.eaglesakura.armyknife.android.gms.error.PlayServiceConnectException
import com.eaglesakura.armyknife.android.gms.error.PlayServiceException
import com.eaglesakura.armyknife.android.gms.error.RequireRetryConnectException
import com.eaglesakura.armyknife.android.gms.error.SignInFailedException
import com.eaglesakura.armyknife.android.gms.error.SignInRequiredException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * GoogleApiClient build with awaitWithSuspend.
 * @see GoogleApiClient.SIGN_IN_MODE_REQUIRED
 * @see GoogleApiClient.SIGN_IN_MODE_OPTIONAL
 */
@Throws(
    DeveloperImplementFailedException::class,
    RequireRetryConnectException::class,
    PlayServiceException::class
)
suspend fun GoogleApiClient.Builder.connect(mode: Int): GoogleApiClient {
    return when (mode) {
        GoogleApiClient.SIGN_IN_MODE_REQUIRED, GoogleApiClient.SIGN_IN_MODE_OPTIONAL -> {
            try {
                connectImpl(mode)
            } catch (err: CancellationException) {
                throw err
            } catch (err: Exception) {
                connectImpl(GoogleApiClient.SIGN_IN_MODE_OPTIONAL)
            }
        }
        else -> throw IllegalArgumentException("Mode error[$mode]")
    }
}

/**
 * GoogleApiClient build with awaitWithSuspend.
 */
@Throws(
    DeveloperImplementFailedException::class,
    RequireRetryConnectException::class,
    PlayServiceException::class
)
suspend fun GoogleApiClient.Builder.connect(): GoogleApiClient {
    return connectImpl(0x00FF00FF)
}

@Throws(
    DeveloperImplementFailedException::class,
    RequireRetryConnectException::class,
    PlayServiceException::class
)
private suspend fun GoogleApiClient.Builder.connectImpl(mode: Int): GoogleApiClient {
    val channel = Channel<Pair<Bundle?, Exception?>>(1)
    val connectionCallbacks = object : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        override fun onConnected(connectionHint: Bundle?) {
            GlobalScope.launch(Dispatchers.Main) {
                channel.send(connectionHint to null)
            }
        }

        override fun onConnectionSuspended(cause: Int) {
            GlobalScope.launch(Dispatchers.Main) {
                channel.send(null to RequireRetryConnectException(cause))
            }
        }

        override fun onConnectionFailed(result: ConnectionResult) {
            GlobalScope.launch(Dispatchers.Main) {
                channel.send(
                    when (result.errorCode) {
                        ConnectionResult.DEVELOPER_ERROR -> null to DeveloperImplementFailedException(result)
                        ConnectionResult.SIGN_IN_REQUIRED -> null to SignInRequiredException(result)
                        ConnectionResult.SIGN_IN_FAILED -> null to SignInFailedException(result)
                        else -> null to PlayServiceConnectException(result)
                    }
                )
            }
        }
    }

    val client = build()
    client.registerConnectionCallbacks(connectionCallbacks)
    client.registerConnectionFailedListener(connectionCallbacks)
    try {
        withContext(Dispatchers.Main) {
            when (mode) {
                GoogleApiClient.SIGN_IN_MODE_REQUIRED, GoogleApiClient.SIGN_IN_MODE_OPTIONAL -> client.connect(mode)
                else -> client.connect()
            }
        }

        val result = channel.receive()
        if (result.second != null) {
            throw result.second!!
        } else {
            return client
        }
    } catch (err: Exception) {
        GlobalScope.launch(Dispatchers.Main + NonCancellable) {
            try {
                client.disconnect()
            } catch (err: Exception) {
            }
        }
        throw err
    } finally {
        client.unregisterConnectionFailedListener(connectionCallbacks)
        client.unregisterConnectionCallbacks(connectionCallbacks)
    }
}

/**
 * GoogleApiClient auto disconnecting block.
 */
suspend fun <T> GoogleApiClient.use(action: suspend (client: GoogleApiClient) -> T): T {
    return try {
        action(this)
    } finally {
        withContext(NonCancellable + Dispatchers.Main) {
            try {
                disconnect()
            } catch (err: Exception) {
                // drop error.
            }
        }
    }
}
