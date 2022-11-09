package com.merative.healthpass.models.api

import java.util.*
import kotlin.collections.ArrayList

class CircularQueue<E>(capacity: Int) : LinkedList<E>() {
    private var capacity = 10

    init {
        this.capacity = capacity
    }

    override fun add(element: E): Boolean {
        if (size >= capacity)
            removeFirst()
        return super.add(element)
    }

    fun toArrayList(): ArrayList<E> {
        val list = ArrayList<E>()
        if (isNotEmpty()) {
            forEach { list.add(it) }
        }
        return list
    }
}