package com.eaglesakura.armyknife.android.gms.error

import com.google.android.gms.common.ConnectionResult

/**
 * Required sign-in
 */
abstract class SignInException(connectionResult: ConnectionResult) : PlayServiceConnectException(connectionResult)
