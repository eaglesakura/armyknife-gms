package com.eaglesakura.armyknife.android.gms

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

typealias InstanceId = InstanceIdResult

typealias FirebaseUserAuthToken = GetTokenResult

/**
 * Firebase current state.
 */
class FirebaseContextSnapshot internal constructor(
    /**
     * Current User.
     */
    val user: FirebaseUser?,

    /**
     * InstanceId snapshot.
     */
    val instanceId: InstanceId?,

    /**
     * User authentication token.
     */
    val userAuthToken: FirebaseUserAuthToken?,

    /**
     * Remote config values.
     */
    val remoteConfigValues: Map<String, FirebaseRemoteConfigValue>,

    /**
     * Remote config fetch status.
     */
    val remoteConfigFetchStatus: Int
) {
    /**
     * Snapshot Date.
     */
    val date: Date = Date()

    /**
     * Snapshot Unique ID.
     * Increment global number on new instance.
     */
    val id: Long = uid.getAndIncrement()

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "FirebaseContextSnapshot(id=$id, user='${user?.uid}', instanceId='${instanceId?.id?.hashCode()}.${instanceId?.token?.hashCode()}', userAuthToken='${userAuthToken.hashCode()}', remoteConfigValues=${remoteConfigValues.keys})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FirebaseContextSnapshot

        if (user != other.user) return false
        if (instanceId != other.instanceId) return false
        if (userAuthToken != other.userAuthToken) return false
        if (remoteConfigValues != other.remoteConfigValues) return false
        if (date != other.date) return false
        if (id != other.id) return false

        return true
    }

    companion object {
        private val uid = AtomicLong()
    }
}