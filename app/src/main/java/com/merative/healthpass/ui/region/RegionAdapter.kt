package com.merative.healthpass.ui.region

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.merative.healthpass.R
import com.merative.healthpass.models.region.Env
import com.merative.healthpass.models.region.getEnvironmentTitle
import com.merative.healthpass.ui.common.recyclerView.RecyclerViewBaseAdapter
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder

class RegionAdapter(private val viewModel: RegionSelectionVM) : RecyclerViewBaseAdapter<Env>() {

    override fun getLayoutResId(viewType: Int): Int = R.layout.btn_radio_description

    override fun bindData(holder: SimpleViewHolder<Env>, model: Env, position: Int) {
        val view = holder.itemView

        val txtTitle = view.findViewById<TextView>(R.id.txt_title)
        val txtDescription = view.findViewById<TextView>(R.id.txt_description)
        val divider = view.findViewById<View>(R.id.divider_line)

        txtTitle.text = model.getEnvironmentTitle(holder.itemView.resources)

        if (model == viewModel.currentEnv) {
            txtTitle.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.outline_check_24,
                0
            )
        } else {
            txtTitle.setCompoundDrawables(null, null, null, null)
        }

        val description = when {
            model.description != null -> model.description
            model.descriptionRes != null -> holder.itemView.resources.getString(model.descriptionRes)
            !model.isProd -> getDescription(model)
            else -> ""
        }
        txtDescription.isVisible = description.isNotBlank()
        txtDescription.text = description

        divider.isVisible = isNotLast(position)

        view.setOnClickListener {
            val isCurrent = model == viewModel.currentEnv
            if (!isCurrent) {
                txtTitle.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.outline_check_24,
                    0
                )
            } else {
                txtTitle.setCompoundDrawables(null, null, null, null)
            }

            if (viewModel.currentEnv != model) {

//                dataList.find { viewModel.currentRegion == it }?.isChecked = false

                viewModel.currentEnv = model
                notifyDataSetChanged()
            } else {
                viewModel.currentEnv = null
            }

            clickedEvent.onNext(holder.itemView to model)
        }
    }

    private fun getDescription(env: Env): String {
        val first = env.url.substring(env.url.indexOfFirst { it == '/' } + 1, env.url.length)
        val second = first.substring(first.indexOfFirst { it == '/' } + 1, first.length)
        return second.substring(0, second.indexOfFirst { it == '/' })
    }

    override fun setViewClickListener(view: View, model: Env, position: Int) {}
}