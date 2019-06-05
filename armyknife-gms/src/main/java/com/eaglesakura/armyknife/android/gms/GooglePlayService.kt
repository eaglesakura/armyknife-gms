@file:Suppress("MemberVisibilityCanBePrivate")

package com.eaglesakura.armyknife.android.gms

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.eaglesakura.armyknife.android.extensions.awaitInCoroutines
import com.eaglesakura.armyknife.android.gms.error.PlayServiceException
import com.eaglesakura.armyknife.android.gms.error.PlayServiceNotAvailableException
import com.eaglesakura.armyknife.android.gms.extensions.connect
import com.eaglesakura.armyknife.android.gms.extensions.use
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient

@Suppress("unused")
/**
 * PlayService系のUtil
 */
object GooglePlayService {
    /**
     * ログインを行うためのIntentを発行する。
     *
     * 既にログイン済みの場合、アクセスを一旦signOutして再度ログインを促すようにする
     *
     * @param builder ログイン対象のAPI
     * @return ログイン用intent
     */
    @Throws(PlayServiceException::class)
    suspend fun newSignInIntent(builder: GoogleApiClient.Builder, withSignOut: Boolean = true): Intent {
        return builder.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL).use { client ->
            if (withSignOut) {
                Auth.GoogleSignInApi.signOut(client).awaitInCoroutines()
            }

            return@use Auth.GoogleSignInApi.getSignInIntent(client)
        }
    }

    /**
     * @param email UserEmail
     * @param scope Access scope.
     */
    fun getOAuth2Token(context: Context, email: String, scope: String): String? {
        return GoogleAuthUtil.getToken(
            context,
            Account(email, "com.google"),
            "oauth2:$scope"
        )
    }

    /**
     * 必須バージョンがインストールされているか確認する
     */
    fun isInstalledRequireVersion(context: Context): Boolean {
        // Google Play Serviceのバージョンチェックを行う
        val instance = GoogleApiAvailability.getInstance()
        val playServiceError = instance.isGooglePlayServicesAvailable(context)
        return playServiceError == ConnectionResult.SUCCESS
    }

    /**
     * 必須バージョンがインストールされていることを確認し、そうでないなら例外を投げる
     */
    @Throws(PlayServiceNotAvailableException::class)
    fun assertInstalledRequireVersion(context: Context) {
        // Google Play Serviceのバージョンチェックを行う
        val instance = GoogleApiAvailability.getInstance()
        val playServiceError = instance.isGooglePlayServicesAvailable(context)
        if (playServiceError != ConnectionResult.SUCCESS) {
            throw PlayServiceNotAvailableException(instance, playServiceError)
        }
    }

    /**
     * Google Play ServiceのインストールIntentを生成する
     */
    fun getGooglePlayServiceInstallIntent(context: Context): Intent {
        return newGooglePlayInstallIntent(context, "com.google.android.gms")
    }

    /**
     * GooglePlayでのインストールIntentを生成する
     */
    fun newGooglePlayInstallIntent(@Suppress("UNUSED_PARAMETER") context: Context, packageName: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=$packageName")
        return intent
    }
}
