package com.merative.watson.healthpass.interfaces

interface Consumer<T> {
    fun onNext(result: T)
}