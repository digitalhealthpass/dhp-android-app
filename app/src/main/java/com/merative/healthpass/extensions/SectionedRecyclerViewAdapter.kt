package com.merative.healthpass.extensions

import com.merative.healthpass.ui.common.recyclerView.BaseSection
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

inline fun <reified T> SectionedRecyclerViewAdapter.addIfNotExist(
    section: BaseSection<T>,
    index: Int = -1
) {
    val loadedSection = getSection(section.tag)
    if (loadedSection == null && index == -1)
        addSection(section.tag, section)
    else if (loadedSection == null && index > -1) {
        addSection(index, section.tag, section)
    }
}