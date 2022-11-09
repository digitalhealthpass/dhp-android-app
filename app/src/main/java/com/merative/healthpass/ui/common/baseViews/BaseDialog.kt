package com.merative.healthpass.ui.common.baseViews

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.merative.healthpass.R


abstract class BaseDialog : DialogFragment() {

    @get:StringRes
    protected abstract val titleId: Int

    @get:LayoutRes
    protected abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.AlertDialogMaterialTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(layoutId, null)

        addCustomBehavior(view)

        return AlertDialog
            .Builder(requireContext(), R.style.AlertDialogMaterialTheme)
            .setView(view)
            .setTitle(requireContext().getString(titleId))
            .create()
    }

    protected abstract fun addCustomBehavior(view: View?)

    /* protected abstract fun getPositiveButtonListener()

     protected abstract fun getNegativeButtonListener()*/
}