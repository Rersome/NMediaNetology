package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentImagePreviewBinding

@AndroidEntryPoint
class ImagePreviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImagePreviewBinding.inflate(inflater, container, false)

        val imageUrl = arguments?.getString("IMAGE_URL")

        Glide.with(this)
            .load(imageUrl)
            .into(binding.fullscreenImage)

        return binding.root
    }
}