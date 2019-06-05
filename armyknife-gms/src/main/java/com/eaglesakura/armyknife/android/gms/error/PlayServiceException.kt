package com.eaglesakura.armyknife.android.gms.error

open class PlayServiceException : Exception {
    constructor()

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super(cause)
}
