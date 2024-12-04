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
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.DetailedFragmentCardPostBinding
import ru.netology.nmedia.util.LongArg
import ru.netology.nmedia.viewmodel.PostViewModel

class PostDetailFragment: Fragment() {
    private lateinit var binding: DetailedFragmentCardPostBinding

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DetailedFragmentCardPostBinding.inflate(inflater, container, false)

        val postId = arguments?.idArg ?: -1
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val post = posts.find { it.id == postId } ?: return@observe
            with (binding) {
                binding.cardPost.author.text = post.author
                binding.cardPost.content.text = post.content
                binding.cardPost.published.text = post.published
                binding.cardPost.Likes.text = post.likes.toString()
                binding.cardPost.Reposts.text = post.reposts.toString()
                binding.cardPost.Likes.isChecked = post.likedByMe
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