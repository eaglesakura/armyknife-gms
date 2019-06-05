@file:Suppress("unused")

package com.eaglesakura.armyknife.android.gms.error

import android.app.Activity
import android.app.PendingIntent
import android.content.IntentSender
import com.google.android.gms.common.ConnectionResult

/**
 * Google Play Serviceの接続に関連した例外
 *
 * 参考: https://developers.google.com/android/reference/com/google/android/gms/common/ConnectionResult.html
 */
open class PlayServiceConnectException(@Suppress("MemberVisibilityCanBePrivate") val connectionResult: ConnectionResult) :
    PlayServiceException() {

    val errorCode: Int
        get() = connectionResult.errorCode

    val resolution: PendingIntent?
        get() = connectionResult.resolution

    val errorMessage: String?
        get() = connectionResult.errorMessage

    val hasResolution: Boolean
        get() = connectionResult.hasResolution()

    @Throws(IntentSender.SendIntentException::class)
    fun startResolutionForResult(activity: Activity, requestCode: Int) {
        connectionResult.startResolutionForResult(activity, requestCode)
    }
}
