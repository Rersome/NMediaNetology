package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.DetailedFragmentCardPostBinding
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.util.LongArg
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PostDetailFragment() : Fragment() {

    @Inject
    lateinit var appDb: AppDb

    private val avatarUrl = "http://10.0.2.2:9999/avatars/"
    private val imageUrl = "http://10.0.2.2:9999/media/"

    private var _binding: DetailedFragmentCardPostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailedFragmentCardPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showUI()
    }

    private fun showUI() {
        val postId = arguments?.idArg ?: -1
        if (postId == -1L) {
            findNavController().navigateUp()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appDb.postDao().getById(postId).firstOrNull()?.map { post ->
                    with(binding) {
                        binding.cardPost.author.text = post.author
                        binding.cardPost.content.text = post.content
                        binding.cardPost.published.text = post.published.toString()
                        binding.cardPost.Likes.text = post.likes.toString()
                        binding.cardPost.Reposts.text = post.reposts.toString()
                        binding.cardPost.Likes.isChecked = post.likedByMe

                        Glide.with(this@PostDetailFragment)
                            .load(avatarUrl + post.authorAvatar)
                            .timeout(30_000)
                            .circleCrop()
                            .placeholder(R.drawable.ic_baseline_data_usage_24)
                            .error(R.drawable.ic_baseline_cancel_24)
                            .into(cardPost.avatar)

                        if (post.attachment != null) {
                            Glide.with(this@PostDetailFragment)
                                .load(imageUrl + post.attachment.url)
                                .timeout(30_000)
                                .placeholder(R.drawable.ic_baseline_data_usage_24)
                                .error(R.drawable.ic_baseline_cancel_24)
                                .into(cardPost.descriptionImage)

                            binding.cardPost.descriptionImage.visibility = View.VISIBLE
                        } else {
                            binding.cardPost.descriptionImage.visibility = View.GONE
                        }

                        binding.cardPost.Likes.setOnClickListener {
                            viewModel.likeById(postId)
                        }
                        binding.cardPost.Reposts.setOnClickListener {
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, post.content)
                                type = "text/plain"
                            }
                            val shareIntent =
                                Intent.createChooser(
                                    intent,
                                    getString(R.string.chooser_share_post)
                                )
                            startActivity(shareIntent)
                            viewModel.shareById(postId)
                        }
                        binding.cardPost.menu.setOnClickListener {
                            PopupMenu(it.context, it).apply {
                                inflate(R.menu.menu_post)
                                setOnMenuItemClickListener { item ->
                                    when (item.itemId) {
                                        R.id.remove -> {
                                            viewModel.removeById(post.id)
                                            findNavController().navigateUp()
                                            true
                                        }

                                        R.id.edit -> {
                                            viewModel.edit(post.toDto())
                                            findNavController().navigate(
                                                R.id.action_detailedFragmentCardPost_to_newPostFragment,
                                                Bundle().apply {
                                                    textArg = post.content
                                                }
                                            )
                                            true
                                        }

                                        else -> false
                                    }
                                }
                            }.show()
                        }
                    }
                }
            }
        }
    }

    companion object {
        var Bundle.idArg by LongArg
    }
}