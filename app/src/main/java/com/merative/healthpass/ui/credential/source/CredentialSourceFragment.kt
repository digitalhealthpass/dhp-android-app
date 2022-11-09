package com.merative.healthpass.ui.credential.source

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.merative.healthpass.databinding.FragmentCredentialSourceBinding
import com.merative.healthpass.ui.common.baseViews.BaseFragment

class CredentialSourceFragment : BaseFragment() {
    private var _binding: FragmentCredentialSourceBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentCredentialSourceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateSourceTextView()
    }

    private fun populateSourceTextView() {
        val credentialJsonString = arguments?.get(KEY_CREDENTIAL) as String? ?: return
        binding.sourceJsonTextview.text = credentialJsonString
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_CREDENTIAL = "credential"
    }
}