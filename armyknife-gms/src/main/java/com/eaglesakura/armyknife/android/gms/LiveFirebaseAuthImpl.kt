package com.eaglesakura.armyknife.android.gms

import androidx.lifecycle.LiveData
import com.eaglesakura.armyknife.android.extensions.awaitInCoroutines
import com.eaglesakura.armyknife.android.gms.GooglePlayService.coroutineScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal object LiveFirebaseAuthImpl : LiveData<FirebaseAuthSnapshot>() {
    override fun onActive() {
        super.onActive()
        Firebase.auth?.addAuthStateListener(listener)
        Firebase.auth?.addIdTokenListener(listener)
    }

    override fun onInactive() {
        Firebase.auth?.removeIdTokenListener(listener)
        Firebase.auth?.removeAuthStateListener(listener)
        super.onInactive()
    }

    internal fun refresh(auth: FirebaseAuth) =
        coroutineScope.launch(Dispatchers.Main) {
            value = if (auth.currentUser == null) {
                FirebaseAuthSnapshot(
                    user = null,
                    token = null
                )
            } else {
                val task = auth.getAccessToken(false).awaitInCoroutines()
                FirebaseAuthSnapshot(
                    user = auth.currentUser,
                    token = task.result!!
                )
            }
        }

    private val listener = object : FirebaseAuth.AuthStateListener, FirebaseAuth.IdTokenListener {
        override fun onAuthStateChanged(auth: FirebaseAuth) {
            refresh(auth)
        }

        override fun onIdTokenChanged(auth: FirebaseAuth) {
            refresh(auth)
        }
    }
}

/**
 * Firebase Auth snapshot.
 */
data class FirebaseAuthSnapshot internal constructor(
    val user: FirebaseUser?,

    val token: GetTokenResult?
) {
    /**
     * Modified date.
     */
    val date = Date()
}

/**
 * Convert to LiveData
 * e.g.)
 * val auth = ...
 * auth.toLiveData().observe { snapshot ->
 *  if(snapshot != null) {
 *      // do something.
 *  }
 * }
 */
fun FirebaseAuth.toLiveData(): LiveData<FirebaseAuthSnapshot> {
    LiveFirebaseAuthImpl.refresh(this)
    return LiveFirebaseAuthImpl
}
