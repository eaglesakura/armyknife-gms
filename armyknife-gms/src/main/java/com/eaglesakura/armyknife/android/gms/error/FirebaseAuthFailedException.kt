package com.eaglesakura.armyknife.android.gms.error

/**
 * Firebaseの認証に失敗した
 */
@Suppress("unused")
class FirebaseAuthFailedException : PlayServiceException {
    constructor()

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super(cause)
}
