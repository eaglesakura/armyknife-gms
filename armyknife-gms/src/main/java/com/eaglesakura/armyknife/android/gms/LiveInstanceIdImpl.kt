package com.eaglesakura.armyknife.android.gms

import androidx.lifecycle.LiveData
import com.eaglesakura.armyknife.android.extensions.awaitInCoroutines
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal object LiveInstanceIdImpl : LiveData<InstanceIdResult>() {

    private var job: Job? = null

    override fun onActive() {
        super.onActive()
        if (job == null && value == null) {
            job = loadInstanceId()
        }
    }

    private fun loadInstanceId() = GlobalScope.launch(Dispatchers.Main) {
        val instanceId = Firebase.instanceId ?: return@launch

        do {
            try {
                val task = instanceId.instanceId.awaitInCoroutines()
                if (task.isSuccessful) {
                    value = task.result
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            delay(TimeUnit.SECONDS.toMillis(1))
        } while (isActive)
    }
}

/**
 * Convert live data.
 *
 * e.g.
 * val instanceId = ...
 * instanceId.toLiveData().observe { token ->
 *  if(token != null) {
 *      // do something
 *  }
 * }
 */
fun FirebaseInstanceId.toLiveData(): LiveData<InstanceIdResult> = LiveInstanceIdImpl
