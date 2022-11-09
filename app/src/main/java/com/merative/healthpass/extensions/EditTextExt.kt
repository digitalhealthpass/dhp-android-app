package com.merative.healthpass.extensions

import android.view.inputmethod.EditorInfo
import android.widget.EditText
import io.reactivex.rxjava3.core.Observable

fun EditText.onImeActionDoneClicks(): Observable<Unit> {
    return Observable.create { emitter ->
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                emitter.onNext(Unit)
                true
            } else {
                false
            }
        }
    }
}

fun EditText.onDone(callback: () -> Unit) {
    // These lines optional if you don't want to set in Xml
    imeOptions = EditorInfo.IME_ACTION_DONE
    maxLines = 1
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            true
        }
        false
    }
}