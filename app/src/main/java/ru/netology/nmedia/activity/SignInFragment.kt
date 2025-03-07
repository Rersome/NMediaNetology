package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentSigninBinding
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSigninBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSigninBinding.inflate(inflater, container, false)

        binding.buttonLogin.setOnClickListener {
            val login = binding.editTextLogin.text.toString()
            val pass = binding.editTextPassword.text.toString()

            authenticateUser(login, pass)
        }

        binding.buttonReg.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        return binding.root
    }

    private fun authenticateUser(login: String, pass: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = PostApi.service.updateUser(login, pass)

                if (response.isSuccessful) {
                    response.body()?.let { token ->
                        AppAuth.getInstance().setAuth(token)
                        findNavController().navigateUp()
                    }
                } else {
                    Snackbar.make(
                        binding.root,
                        "Введен неправильный логин пользователя или пароль.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiError) {
                throw e
            } catch (_: Exception) {
                throw UnknownError
            } catch (_: IOException) {
                throw NetworkError
            }
        }
    }
}