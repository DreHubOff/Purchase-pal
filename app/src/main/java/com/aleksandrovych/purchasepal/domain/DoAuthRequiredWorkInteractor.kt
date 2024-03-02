package com.aleksandrovych.purchasepal.domain

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DoAuthRequiredWorkInteractor @Inject constructor() {

    suspend operator fun <T : Any> invoke(work: suspend () -> T): T {
        checkAuth()
        return work()
    }

    private suspend fun checkAuth() {
        if (Firebase.auth.currentUser == null) {
            Firebase.auth.signInAnonymously().await()
        }
    }
}