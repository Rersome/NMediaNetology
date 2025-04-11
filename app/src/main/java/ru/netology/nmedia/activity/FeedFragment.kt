package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.activity.PostDetailFragment.Companion.idArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedError
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        fun showLoginDialog() {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Вы не вошли в свой профиль")
                .setMessage("Пожалуйста, зайдите в свой профиль")
                .setPositiveButton("OK") { _, _ ->
                    findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val viewModel: PostViewModel by viewModels()

        val adapter = PostsAdapter(object : OnInteractionListener {

            override fun onLike(post: Post) {
                if (appAuth.authState.value?.token == null) {
                    showLoginDialog()
                } else {
                    viewModel.likeById(post.id)
                }
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
                viewModel.shareById(post.id)
            }

            override fun onPostClick(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_detailedFragmentCardPost,
                    Bundle().apply {
                        idArg = post.id
                    }
                )
            }

            override fun onImagePreview(post: Post) {
                val imageUrl = "http://10.0.2.2:9999/media/"
                val bundle = Bundle().apply {
                    putString("IMAGE_URL", imageUrl + post.attachment?.url)
                }
                findNavController().navigate(
                    R.id.action_feedFragment_to_imagePreviewFragment,
                    bundle
                )
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    }
                )
            }

        })
        binding.list.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swipeRefresh.isRefreshing
                    state.refresh is LoadState.Loading ||
                            state.prepend is LoadState.Loading ||
                            state.append is LoadState.Loading
                }
            }
        }

        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)

//        viewModel.newerCount.observe(viewLifecycleOwner) { count ->
//            if (count > 0) {
//                binding.newPosts.visibility = View.VISIBLE
//            }
//        }


        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error != FeedError.NONE) {
                Snackbar.make(binding.root, "Error: ${state.error}", Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) {
                        viewModel.loadPosts()
                    }
                    .setAnchorView(binding.fab)
                    .show()
            }
            binding.progress.isVisible = state.loading
        }

        binding.newPosts.setOnClickListener {
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        binding.list.smoothScrollToPosition(0)
                    }
                }
            })
            binding.newPosts.visibility = View.GONE
            viewModel.showNewPosts()
        }

        binding.retry.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.fab.setOnClickListener {
            if (appAuth.authState.value?.token == null) {
                showLoginDialog()
            } else {
                viewModel.cancelEdit()
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }
        }
        return binding.root
    }
}