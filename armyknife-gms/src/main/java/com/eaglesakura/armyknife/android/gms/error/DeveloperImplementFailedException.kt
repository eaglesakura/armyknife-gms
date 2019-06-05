package com.eaglesakura.armyknife.android.gms.error

import com.google.android.gms.common.ConnectionResult

/**
 * 開発者による実装エラーである場合に投げられる
 */
class DeveloperImplementFailedException(connectionResult: ConnectionResult) :
    PlayServiceConnectException(connectionResult)