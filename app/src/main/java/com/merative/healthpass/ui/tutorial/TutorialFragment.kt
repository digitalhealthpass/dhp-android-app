package com.merative.healthpass.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayoutMediator
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragViewPager2Binding
import com.merative.healthpass.extensions.hasNext
import com.merative.healthpass.extensions.next
import com.merative.healthpass.extensions.rxError
import com.merative.healthpass.ui.common.baseViews.BaseBottomSheet
import com.merative.healthpass.utils.RxHelper
import io.reactivex.rxjava3.disposables.SerialDisposable

class TutorialFragment : BaseBottomSheet() {
    private var _binding: FragViewPager2Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val clickDisposable = SerialDisposable()
    private lateinit var viewModel: TutorialVM
    private lateinit var adapter: TutorialAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragViewPager2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(TutorialVM::class.java)

        adapter = TutorialAdapter()
        binding.viewpager2.adapter = adapter

        TabLayoutMediator(binding.intoTabLayout, binding.viewpager2)
        { tab, position ->
            tab.contentDescription =
                getString(R.string.tutorial_accessibility_pager_indicator, position + 1)
        }.attach()
    }

    override fun onResume() {
        super.onResume()
        clickDisposable.set(
            adapter.listenToClickEvents()
                .subscribe({
                    when {
                        binding.viewpager2.hasNext() -> {
                            binding.viewpager2.next()
                        }
                        else -> {
                            viewModel.tutorialDone()
                            setResult(true)
                        }
                    }
                }, rxError("failed to listen to clicks"))
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        RxHelper.unsubscribe(clickDisposable.get())
        _binding = null
    }
}