package com.eaglesakura.armyknife.android.gms

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eaglesakura.armyknife.android.ApplicationRuntime
import com.eaglesakura.armyknife.android.extensions.awaitInCoroutines
import com.eaglesakura.armyknife.android.junit4.extensions.compatibleTest
import com.eaglesakura.armyknife.android.junit4.extensions.instrumentationBlockingTest
import com.eaglesakura.armyknife.android.junit4.extensions.instrumentationTest
import com.eaglesakura.armyknife.android.junit4.extensions.localTest
import com.eaglesakura.armyknife.android.junit4.extensions.testContext
import com.eaglesakura.armyknife.runtime.extensions.send
import com.google.firebase.iid.InstanceIdResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseTest {

    @Before
    fun before() {
        if (ApplicationRuntime.runIn(ApplicationRuntime.RUNTIME_INSTRUMENTATION)) {
            Firebase.provideFromAssets(testContext, "google-services.json")
        }
    }

    @After
    fun after() {
        Firebase.auth?.signOut()
    }

    @Test
    fun checkLinking() = compatibleTest {
        assertTrue(Firebase.linkAppModule)
        assertTrue(Firebase.linkAuthModule)
        assertTrue(Firebase.linkFirestoreModule)
        assertTrue(Firebase.linkInstanceIdModule)
        assertTrue(Firebase.linkRemoteConfigModule)
        assertTrue(Firebase.linkStorageModule)
        assertTrue(Firebase.linkAnalyticsModule)
    }

    @Test
    fun getInstances_instrumentation() = instrumentationTest {
        assertNotNull(Firebase.app)
        assertNotNull(Firebase.auth)
        assertNotNull(Firebase.firestore)
        assertNotNull(Firebase.storage())
        assertNotNull(Firebase.instanceId)
        assertNotNull(Firebase.remoteConfig)
    }

    @Test
    fun getInstances_robolectric() = localTest {
        assertNull(Firebase.app)
        assertNull(Firebase.auth)
        assertNull(Firebase.firestore)
        assertNull(Firebase.storage())
        assertNull(Firebase.instanceId)
        assertNull(Firebase.remoteConfig)
    }

    @Test
    fun getInstanceId() = instrumentationBlockingTest(Dispatchers.Main) {
        val liveData = Firebase.instanceId!!.toLiveData()
        val channel = Channel<InstanceIdResult>()
        liveData.observeForever {
            if (it != null) {
                channel.send(Dispatchers.Main, it)
            }
        }

        assertNotNull(channel.receive().let {
            Log.d(javaClass.simpleName, "InstanceId='${it.id}', Token='${it.token}'")
        })
    }

    @Test
    fun auth() = instrumentationBlockingTest(Dispatchers.Main) {
        Firebase.auth!!.signInAnonymously().awaitInCoroutines()
        val liveData = Firebase.auth!!.toLiveData()
        val channel = Channel<FirebaseAuthSnapshot>()
        liveData.observeForever {
            if (it?.user != null) {
                channel.send(Dispatchers.Main, it)
            }
        }
        assertNotNull(channel.receive().also { snapshot ->
            Log.d(
                    javaClass.simpleName,
                    "Date='${snapshot.date}', User='${snapshot.user?.uid}', Token='${snapshot.token?.token}'"
            )
        })
    }

    @Test
    fun remoteConfig() = instrumentationBlockingTest(Dispatchers.Main) {
        Firebase.remoteConfig!!.fetchAndActivate().awaitInCoroutines()
        assertEquals("hello_world", Firebase.remoteConfig!!.getString("example"))
    }
}