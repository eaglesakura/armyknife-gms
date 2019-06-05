package com.eaglesakura.armyknife.android.gms

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration

internal class LiveDocumentSnapshotImpl(
    private val ref: DocumentReference
) : LiveData<DocumentSnapshot>() {

    init {
        ref.get().addOnCompleteListener {
            value = it.result
        }
    }

    private var register: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()
        register = ref.addSnapshotListener(listener)
    }

    override fun onInactive() {
        register?.remove()
        register = null
        super.onInactive()
    }

    private val listener = EventListener<DocumentSnapshot> { snapshot, _ ->
        if (snapshot != null) {
            value = snapshot
        }
    }
}

/**
 * Convert live data.
 * e.g.)
 * val doc = ...
 * doc.toLiveData().observe { snapshot ->
 *  if(snapshot != null) {
 *      // do something
 *  }
 * }
 */
fun DocumentReference.toLiveData(): LiveData<DocumentSnapshot> = LiveDocumentSnapshotImpl(this)