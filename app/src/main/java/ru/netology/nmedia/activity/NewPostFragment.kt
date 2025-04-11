package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        arguments?.textArg?.let(binding.edit::setText)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        choiceOfText()
        photoObserver()
        setupListeners()
    }

    private fun choiceOfText() {
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.new_post_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    if (menuItem.itemId == R.id.save) {
                        val postText = binding.edit.text.toString()
                        if (postText.isNotBlank()) {
                            viewModel.applyChangeAndSave(postText)
                            AndroidUtils.hideKeyboard(requireView())
                        }
                        true
                    } else {
                        false
                    }
            }, viewLifecycleOwner)
    }

    private fun photoObserver() {
        viewModel.photo.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.previewContainer.isGone = true
                return@observe
            }
            binding.previewContainer.isVisible = true
            binding.preview.setImageURI(it.uri)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            findNavController().navigateUp()
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

        binding.pickPhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .maxResultSize(2048, 2048)
                .crop()
                .galleryOnly()
                .createIntent {
                    photoResultContract.launch(it)
                }
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.Builder(this)
                .maxResultSize(2048, 2048)
                .crop()
                .cameraOnly()
                .createIntent {
                    photoResultContract.launch(it)
                }
        }

        binding.clear.setOnClickListener {
            viewModel.savePhoto(null)
        }
    }
    companion object {
        var Bundle.textArg by StringArg
    }
}