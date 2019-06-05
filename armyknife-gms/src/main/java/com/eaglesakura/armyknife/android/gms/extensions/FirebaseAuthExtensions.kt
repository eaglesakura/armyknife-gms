package com.eaglesakura.armyknife.android.gms.extensions

import com.eaglesakura.armyknife.android.extensions.awaitInCoroutines
import com.eaglesakura.armyknife.android.gms.error.FirebaseAuthFailedException
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

/**
 * Firebase auth token with Cached.
 */
private var tokenCache: String? = null

/**
 * Expire time, milli seconds.
 */
private var tokenExpireTime: Long = 0

/**
 * Get Firebase-access-token from GooglePlayService with Managed cache.
 * When after than expired time, This method refresh to tokens.
 */
suspend fun FirebaseAuth.getCachedAccessToken(): String {
    if (!tokenCache.isNullOrEmpty() && System.currentTimeMillis() < tokenExpireTime) {
        // トークンがまだ有効である
        return tokenCache!!
    }

    // トークンをリフレッシュする
    val user = currentUser ?: throw FirebaseAuthFailedException("not authorized")
    val tokenTask = user.getIdToken(true).awaitInCoroutines()
    if (!tokenTask.isSuccessful) {
        throw FirebaseAuthFailedException("getIdToken(true) failed")
    }

    tokenCache = tokenTask.result!!.token
    tokenExpireTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(59)
    return tokenCache!!
}
