package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentImagePreviewBinding
import ru.netology.nmedia.databinding.FragmentImagePreviewBinding.*

@AndroidEntryPoint
class ImagePreviewFragment : Fragment() {

    private var _binding: FragmentImagePreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showImage()
    }

    private fun showImage() {
        val imageUrl = arguments?.getString("IMAGE_URL")

        Glide.with(this)
            .load(imageUrl)
            .into(binding.fullscreenImage)
    }
}