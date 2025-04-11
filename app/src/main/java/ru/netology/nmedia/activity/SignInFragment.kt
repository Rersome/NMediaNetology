package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSigninBinding
import ru.netology.nmedia.viewmodel.AuthViewModel

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private var _binding: FragmentSigninBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSigninBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.authenticationState.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    findNavController().navigateUp()
                } else {
                    val message = it.exceptionOrNull()?.message ?: "Ошибка аутентификации"
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                }
                viewModel.authenticationState.value = null
            }
        }
    }

    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            val login = binding.editTextLogin.text.toString()
            val pass = binding.editTextPassword.text.toString()

            viewModel.authenticateUser(login, pass)
        }

        binding.buttonReg.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }
    }
}