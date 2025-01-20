package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.DetailedFragmentCardPostBinding
import ru.netology.nmedia.util.LongArg
import ru.netology.nmedia.viewmodel.PostViewModel

class PostDetailFragment : Fragment() {
    private lateinit var binding: DetailedFragmentCardPostBinding

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DetailedFragmentCardPostBinding.inflate(inflater, container, false)

        val postId = arguments?.idArg ?: -1
        viewModel.data.observe(viewLifecycleOwner) { state ->
            val post = state.posts.find { it.id == postId } ?: return@observe
            with(binding) {
                binding.cardPost.author.text = post.author
                binding.cardPost.content.text = post.content
                binding.cardPost.published.text = post.published.toString()
                binding.cardPost.Likes.text = post.likes.toString()
                binding.cardPost.Reposts.text = post.reposts.toString()
                binding.cardPost.Likes.isChecked = post.likedByMe

                val avatarUrl = "http://10.0.2.2:9999/avatars/"
                val imageUrl = "http://10.0.2.2:9999/images/"

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
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
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
                                viewModel.edit(post)
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
        return binding.root
    }

    companion object {
        var Bundle.idArg by LongArg
    }
}