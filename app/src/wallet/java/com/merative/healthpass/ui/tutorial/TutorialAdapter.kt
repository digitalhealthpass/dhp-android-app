package com.merative.healthpass.ui.tutorial

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.merative.healthpass.R
import com.merative.healthpass.ui.common.recyclerView.RecyclerViewBaseAdapter
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder

class TutorialAdapter : RecyclerViewBaseAdapter<TutorialData>() {

    init {
        addItem(
            TutorialData(
                R.drawable.helper_image,
                R.string.gs_step0_image_description,
                R.string.gs_step0_title,
                R.string.gs_step0_message
            )
        )
        addItem(
            TutorialData(
                R.drawable.ic_undraw_medicine_b1ol,
                R.string.gs_step1_image_description,
                R.string.gs_step1_title,
                R.string.gs_step1_message
            )
        )
        addItem(
            TutorialData(
                R.drawable.helper_image_two,
                R.string.gs_step2_image_description,
                R.string.gs_step2_title,
                R.string.gs_step2_message
            )
        )
    }

    override fun getItemCount(): Int = 3

    override fun getLayoutResId(viewType: Int): Int = R.layout.frag_tutorial

    override fun bindData(
        holder: SimpleViewHolder<TutorialData>,
        model: TutorialData,
        position: Int
    ) {
        holder.itemView.findViewById<ImageView>(R.id.tutorial_img).apply {
            setImageResource(model.imageResourceId)
            contentDescription =
                holder.itemView.context.getString(model.imageDescriptionStringId)
        }
        holder.itemView.findViewById<TextView>(R.id.tutorial_txt_title).setText(model.titleStringId)
        holder.itemView.findViewById<TextView>(R.id.tutorial_txt_subtitle)
            .setText(model.subTitleStringId)

        holder.itemView.findViewById<Button>(R.id.tutorial_btn_next).apply {
            if (position == itemCount - 1) {
                setText(R.string.gs_finishButtonTitle_getStarted)
            } else {
                setText(R.string.gs_finishButtonTitle_next)
            }
            setOnClickListener {
                clickedEvent.onNext(it to model)
            }
        }
    }

    override fun setViewClickListener(view: View, model: TutorialData, position: Int) {}
}