package com.eaglesakura.armyknife.android.gms.error

import com.google.android.gms.common.ConnectionResult

/**
 * Required sign-in
 */
class SignInFailedException(connectionResult: ConnectionResult) : SignInException(connectionResult)