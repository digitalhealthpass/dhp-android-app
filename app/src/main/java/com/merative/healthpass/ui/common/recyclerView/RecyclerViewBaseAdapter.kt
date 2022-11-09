package com.merative.healthpass.ui.common.recyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.subjects.PublishSubject

abstract class RecyclerViewBaseAdapter<T> : RecyclerView.Adapter<SimpleViewHolder<T>>() {
    var dataList: ArrayList<T> = ArrayList()
    protected val clickedEvent: PublishSubject<Pair<View, T>> = PublishSubject.create()

    //region recycler functions
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder<T> {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(getLayoutResId(viewType), parent, false)
        return SimpleViewHolder(listItem)
    }

    @LayoutRes
    abstract fun getLayoutResId(viewType: Int): Int

    abstract fun bindData(holder: SimpleViewHolder<T>, model: T, position: Int)

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: SimpleViewHolder<T>, position: Int) {
        val model = dataList[position]
        holder.model = model
        setViewClickListener(holder.itemView, model, position)
        bindData(holder, model, position)
    }
    //endregion

    protected open fun setViewClickListener(view: View, model: T, position: Int) {
        view.setOnClickListener {
            clickedEvent.onNext(view to model)
        }
    }

    fun listenToClickEvents(): PublishSubject<Pair<View, T>> = clickedEvent

    protected fun isNotLast(position: Int) = position < itemCount - 1

    //region helpers
    open fun setItems(listItems: ArrayList<T>) {
        dataList = listItems
        notifyDataSetChanged()
    }

    fun addItems(listItems: List<T>) {
        dataList.addAll(listItems)
        notifyItemRangeInserted(dataList.size - listItems.size, listItems.size)
    }

    fun addItem(model: T) {
        dataList.add(model)
        notifyItemRangeInserted(dataList.size - 1, 1)
    }

    fun removeItems(model: T): Boolean {
        val index = dataList.indexOf(model)
        val removed = dataList.remove(model)
        if (removed) {
            notifyItemRemoved(index)
        }
        return removed
    }

    fun removeAllItems(listItems: List<T>) {
        dataList.removeAll(listItems)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): T {
        return dataList[position]
    }
    //endregion
}