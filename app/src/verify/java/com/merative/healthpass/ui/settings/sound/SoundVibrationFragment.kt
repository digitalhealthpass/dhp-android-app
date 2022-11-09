package com.merative.healthpass.ui.settings.sound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.merative.healthpass.databinding.FragSoundVibrationBinding
import com.merative.healthpass.ui.common.baseViews.BaseFragment

class SoundVibrationFragment : BaseFragment() {

    private var _binding: FragSoundVibrationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var viewModel: SoundVibrationVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)

        _binding = FragSoundVibrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(SoundVibrationVM::class.java)

        binding.soundSwitch.isChecked = viewModel.isSoundEnabled()
        binding.vibrationSwitch.isChecked = viewModel.isHapticEnabled()
        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSoundEnabled(isChecked)
        }
        binding.vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setHapticEnabled(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}