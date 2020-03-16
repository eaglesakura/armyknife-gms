package com.eaglesakura.armyknife.android.gms

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eaglesakura.armyknife.android.ApplicationRuntime
import com.eaglesakura.armyknife.android.extensions.awaitInCoroutines
import com.eaglesakura.armyknife.android.junit4.extensions.compatibleBlockingTest
import com.eaglesakura.armyknife.android.junit4.extensions.instrumentationBlockingTest
import com.eaglesakura.armyknife.android.junit4.extensions.targetApplication
import com.eaglesakura.armyknife.android.junit4.extensions.testContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseContextTest {

    @Before
    fun before() {
        if (ApplicationRuntime.runIn(ApplicationRuntime.RUNTIME_INSTRUMENTATION)) {
            Firebase.provideFromAssets(testContext, "google-services.json")
        }
        Firebase.auth?.signOut()
    }

    @After
    fun after() {
        Firebase.auth?.signOut()
    }

    @Test
    fun getInstance() = compatibleBlockingTest(Dispatchers.Main) {
        val instance = FirebaseContext.getInstance(targetApplication)

        // same instance.
        assertEquals(instance, FirebaseContext.getInstance(targetApplication))
    }

    @Test
    fun auth() = instrumentationBlockingTest(Dispatchers.Main) {
        val instance = FirebaseContext.getInstance(targetApplication)

        instance.observeForever { snapshot ->
            Log.d("FirebaseContextTest", "$snapshot")
            Log.d(
                "FirebaseContextTest",
                "user.token='${snapshot.userAuthToken?.token}', instance.id='${snapshot?.instanceId?.id}', instance.token='${snapshot?.instanceId?.token}'"
            )
            Log.d(
                "FirebaseContextTest",
                "remoteconfig.status='${snapshot.remoteConfigFetchStatus}'"
            )
        }

        Firebase.auth!!.signInAnonymously().awaitInCoroutines()
        Log.d("FirebaseContextTest", "signIn done")
        delay(1000 * 15)
        Firebase.auth!!.signOut()
        Log.d("FirebaseContextTest", "signOut done")
        delay(1000 * 15)
    }
}