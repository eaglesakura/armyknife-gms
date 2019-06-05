package com.eaglesakura.armyknife.android.gms.error

class PlayServiceNetworkNotConnectedException : PlayServiceException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}