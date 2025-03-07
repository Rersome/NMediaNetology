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
import ru.netology.nmedia.databinding.FragmentSignupBinding
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        binding.buttonRegister.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val login = binding.editTextLogin.text.toString()
            val pass = binding.editTextPassword.text.toString()
            val confirmPass = binding.editTextConfirmPassword.text.toString()

            if (pass != confirmPass) {
                Snackbar.make(binding.root, "Пароли не совпадают", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            } else {
                registerUser(name, login, pass)
            }
        }
        return binding.root
    }

    private fun registerUser(name: String, login: String, pass: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = PostApi.service.registerUser(login, pass, name)

                if (response.isSuccessful) {
                    response.body()?.let { token ->
                        AppAuth.getInstance().setAuth(token)
                        findNavController().navigate(R.id.feedFragment)
                    }
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