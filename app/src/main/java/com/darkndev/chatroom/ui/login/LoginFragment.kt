package com.darkndev.chatroom.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.darkndev.chatroom.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    //comes from navigation dependency
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.apply {
            signInUsername.setText(viewModel.usernameText)
            signInUsername.doAfterTextChanged {
                viewModel.usernameText = it.toString().trim()
            }

            signInUsernameLayout.setEndIconOnClickListener {
                viewModel.signIn()
            }
            signInUsername.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    viewModel.signIn()
                    return@setOnEditorActionListener true
                }
                false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.receive.collectLatest {
                when (it) {
                    LoginViewModel.Event.NavigateToChatRoom -> {
                        val action =
                            LoginFragmentDirections.actionLoginFragmentToChatFragment(viewModel.usernameText)
                        findNavController().navigate(action)
                    }

                    is LoginViewModel.Event.ShowMessage -> {
                        Snackbar.make(binding.root, it.message, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}