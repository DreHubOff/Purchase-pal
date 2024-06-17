package com.aleksandrovych.purchasepal

import android.content.Intent
import android.net.Uri
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object Deeplink {

    const val scheme = "https"
    const val mimeType = "text/plain"
    const val host = BuildConfig.APP_DOMAIN

    const val sharePath = "share"

    const val dataParameter = "data"
    const val firebaseIdParameter = "firebaseId"

    suspend fun getDynamicDeepLinks(intent: Intent): Uri? =
        Firebase.dynamicLinks.getDynamicLink(intent).await()?.link
}