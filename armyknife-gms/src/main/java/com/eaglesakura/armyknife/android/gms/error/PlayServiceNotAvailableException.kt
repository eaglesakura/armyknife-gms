@file:Suppress("unused")

package com.eaglesakura.armyknife.android.gms.error

import com.google.android.gms.common.GoogleApiAvailability

/**
 * Google Play Serviceの接続に関連した例外
 *
 * 参考: https://developers.google.com/android/reference/com/google/android/gms/common/ConnectionResult.html
 */
class PlayServiceNotAvailableException(private var apiAvailability: GoogleApiAvailability, private var errorCode: Int) :
    PlayServiceException() {
    val errorMessage: String
        get() = apiAvailability.getErrorString(errorCode)
}
