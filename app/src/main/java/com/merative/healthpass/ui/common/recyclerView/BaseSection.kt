package com.merative.healthpass.ui.common.recyclerView

import android.view.View
import com.merative.healthpass.R
import com.merative.healthpass.extensions.getDrawableCompat
import com.merative.watson.healthpass.verifiablecredential.extensions.javaName
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

abstract class BaseSection<T>(
    sectionParameters: SectionParameters
) : Section(sectionParameters) {
    var tag: String? = javaName()
    protected val dataList: MutableList<T> = ArrayList()
    protected val clickEvents = PublishSubject.create<T>()!!

    override fun getContentItemsTotal(): Int = dataList.size

    fun addItem(item: T) {
        dataList.add(item)
    }

    fun addItems(items: List<T>) {
        dataList.addAll(items)
    }

    fun setDataList(dataList: List<T>) {
        this.dataList.clear()
        this.dataList.addAll(dataList)
    }

    fun listenToClickEvents(): Observable<T> = clickEvents

    /** Set background with rounded corners for needed items */
    fun adjustBackgroundForSection(view: View, position: Int) {
        view.apply {
            when {
                dataList.size == 1 -> {
                    background =
                        this.context.getDrawableCompat(R.drawable.square_with_rounded_corner)
                }
                position == 0 -> {
                    background =
                        context.getDrawableCompat(R.drawable.bg_upper_rounded)
                }
                position == dataList.size - 1 -> {
                    background =
                        context.getDrawableCompat(R.drawable.bg_lower_rounded)
                }
            }
        }
    }

    protected fun isLastItem(position: Int) = position == dataList.size - 1
}