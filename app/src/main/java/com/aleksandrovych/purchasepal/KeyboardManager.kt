package com.aleksandrovych.purchasepal

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class KeyboardManager @Inject constructor(@ApplicationContext context: Context) {

    private val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    fun show(view: View) {
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}