package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignupBinding
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.viewmodel.SignUpViewModel

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    val viewModel by viewModels<SignUpViewModel>()

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.photo.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.previewReg.isGone = true
                return@observe
            }
            binding.previewReg.isVisible = true
            binding.previewReg.setImageURI(it.uri)
        }

        viewModel.signUpState.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    findNavController().navigate(R.id.feedFragment)
                } else {
                    val message = it.exceptionOrNull()?.message ?: "Пароли не совпадают"
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                }
                viewModel.signUpState.value = null
            }
        }
    }

    private fun returnContract(): ActivityResultLauncher<Intent> {
        val photoResultContract =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.image_picker_error),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    return@registerForActivityResult
                }
                val uri = it.data?.data ?: return@registerForActivityResult
                viewModel.savePhoto(PhotoModel(uri, uri.toFile()))
            }
        return photoResultContract
    }

    private fun setupListeners() {
        val photoResultContract = returnContract()

        binding.pickPhotoReg.setOnClickListener {
            ImagePicker.Builder(this)
                .maxResultSize(2048, 2048)
                .crop()
                .galleryOnly()
                .createIntent {
                    photoResultContract.launch(it)
                }
        }

        binding.takePhotoReg.setOnClickListener {
            ImagePicker.Builder(this)
                .maxResultSize(2048, 2048)
                .crop()
                .cameraOnly()
                .createIntent {
                    photoResultContract.launch(it)
                }
        }

        binding.buttonRegister.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val login = binding.editTextLogin.text.toString()
            val pass = binding.editTextPassword.text.toString()
            val confirmPass = binding.editTextConfirmPassword.text.toString()

            if (pass != confirmPass) {
                Snackbar.make(binding.root, "Пароли не совпадают", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            } else {
                if (binding.previewReg.isVisible) {
                    viewModel.photo.value?.let {
                        viewModel.registerUserWithPhoto(
                            name, login, pass, it
                        )
                    }
                } else {
                    viewModel.registerUser(name, login, pass)
                }
            }
            viewModel.savePhoto(null)
        }
    }
}