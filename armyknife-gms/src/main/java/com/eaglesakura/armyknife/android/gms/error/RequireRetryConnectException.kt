@file:Suppress("unused")

package com.eaglesakura.armyknife.android.gms.error

/**
 * 何らかの理由で接続が不完全になったため、リトライを要求する
 */
class RequireRetryConnectException(@Suppress("MemberVisibilityCanBePrivate") val playServiceCause: Int) :
    PlayServiceException()
