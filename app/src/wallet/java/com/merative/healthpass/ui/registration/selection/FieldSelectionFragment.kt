package com.merative.healthpass.ui.registration.selection

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragSelectionBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.utils.schema.Field
import com.merative.healthpass.utils.schema.isArray
import com.merative.healthpass.utils.schema.isLocation
import io.reactivex.rxjava3.disposables.SerialDisposable

class FieldSelectionFragment : BaseFragment() {
    private var _binding: FragSelectionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var doneMenuItem: MenuItem? = null

    private val clicksDisposable = SerialDisposable()
    private val selectedList by lazy {
        (arguments?.get(KEY_SELECTED) as? ArrayList<Any>).orValue(ArrayList())
    }
    private val field by lazy { arguments?.get(KEY_FIELD) as Field }

    private lateinit var viewModel: FieldSelectionVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        _binding = FragSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(FieldSelectionVM::class.java)

        val title = if (field.description.isNotEmpty())
            field.description
        else
            field.path.splitCamelCase().capitalize()

        setTitle(title)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            val adapter = FieldSelectionAdapter(field, selectedList)
            this.adapter = adapter
            populateRecycler(adapter, field)

        }
    }

    override fun onResume() {
        super.onResume()
        applyAppBarLayout {
            setExpanded(false)
        }
    }

    private fun populateRecycler(adapter: FieldSelectionAdapter, field: Field) {
        val list = if (field.isLocation()) {
            viewModel.locationList
        } else {
            field.enum
        }

        adapter.setItems(list?.filterNotNull().toArrayList())

        clicksDisposable.set(
            adapter.listenToClickEvents().subscribe({
                if (field.isArray()) {
                    //if it is an array, then make sure to add/remove
                    if (selectedList.contains(it.second)) {
                        //if it was added then remove it
                        selectedList.remove(it.second)
                    } else {
                        when {
                            it.second.toString() == Field.RACE_DECLINE_TO_STATE -> {
                                //if user choose Decline, then remove all selected
                                selectedList.clear()
                                selectedList.add(it.second)
                                adapter.notifyDataSetChanged()
                            }
                            validateMaxSelectedCount() -> {
                                selectedList.add(it.second)
                            }
                            else -> {
                                //update adapter
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                    doneMenuItem?.isVisible = !selectedList.isNullOrEmpty()
                } else {
                    //if it is single select, then make sure to have only one item in the list
                    selectedList.clear()
                    selectedList.add(it.second)
                }

                if (!field.isArray()) {
                    sendValueBack()
                }
            }, rxError("failed to listen to clicks"))
        )
    }

    private fun validateMaxSelectedCount(): Boolean {
        if (selectedList.size == MAX_SELECTED_COUNT) {
            activity?.showDialog(
                field.path.splitCamelCase().capitalize(),
                getString(R.string.reg_confirmation),
                getString(R.string.button_title_ok)
            ) {}
            return false
        } else if (selectedList.contains(Field.RACE_DECLINE_TO_STATE)) {
            activity?.showDialog(
                field.path.splitCamelCase().capitalize(),
                getString(R.string.error_field_decline_selected),
                getString(R.string.button_title_ok)
            ) {}
            return false
        }
        return true
    }

    private fun sendValueBack() {
        setFragmentResult(
            KEY_RESULT, bundleOf(
                KEY_FIELD to field,
                KEY_SELECTED to selectedList
            )
        )
        findNavController().popBackStack()
    }

    //region menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (field.isArray()) {
            inflater.inflate(R.menu.menu_selection, menu)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        doneMenuItem = menu.findItem(R.id.menu_done)
        doneMenuItem?.isVisible = !selectedList.isNullOrEmpty()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_done -> {
                sendValueBack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //endregion

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_FIELD = "field"
        const val KEY_SELECTED = "selected"
        const val KEY_RESULT = "result"
        private const val MAX_SELECTED_COUNT = 3
    }
}