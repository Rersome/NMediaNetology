package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.FragmentCardAdBinding
import ru.netology.nmedia.databinding.FragmentItemLoadingBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.CalculateValues.calculateNumber
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onPostClick(post: Post)
    fun onImagePreview(post: Post)
}


class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {

    private var prependState: LoadState = LoadState.NotLoading(false)
    private var appendState: LoadState = LoadState.NotLoading(false)

    fun setLoadStates(prepend: LoadState, append: LoadState) {
        prependState = prepend
        appendState = append
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when {
        showPrependLoading(position) -> R.layout.fragment_item_loading
        showAppendLoading(position) -> R.layout.fragment_item_loading
        else -> when (getItem(position)) {
            is Ad -> R.layout.fragment_card_ad
            is Post -> R.layout.card_post
            null -> error("unknown item type")
        }
    }

    private fun showPrependLoading(position: Int): Boolean {
        return position == 0 && prependState is LoadState.Loading
    }

    private fun showAppendLoading(position: Int): Boolean {
        return position == itemCount - 1 && appendState is LoadState.Loading
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.fragment_item_loading -> PostLoadingViewHolder(
                FragmentItemLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                {}
            )
            R.layout.card_post -> {
                val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }
            R.layout.fragment_card_ad -> {
                val binding = FragmentCardAdBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AdViewHolder(binding)
            }
            else -> error("unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PostLoadingViewHolder -> holder.bind(
                when {
                    showPrependLoading(position) -> prependState
                    showAppendLoading(position) -> appendState
                    else -> LoadState.NotLoading(false)
                }
            )
            is AdViewHolder -> (getItem(position) as? Ad)?.let { holder.bind(it) }
            is PostViewHolder -> (getItem(position) as? Post)?.let { post ->
                holder.bind(post)
                holder.itemView.setOnClickListener {
                    onInteractionListener.onPostClick(post)
                }
            }
        }
    }
}

private class AdViewHolder(
    private val binding: FragmentCardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        Glide.with(itemView.context)
            .load("http://10.0.2.2:9999/media/${ad.image}")
            .into(binding.image)
    }

}

private class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,

    ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {

        binding.apply {
            author.text = post.author
            content.text = post.content
            published.text = post.published.toString()

            val avatarUrl = "http://10.0.2.2:9999/avatars/"
            val imageUrl = "http://10.0.2.2:9999/media/"

            Glide.with(itemView.context)
                .load(avatarUrl + post.authorAvatar)
                .timeout(30_000)
                .circleCrop()
                .into(avatar)

            if (post.attachment != null) {
                Glide.with(itemView.context)
                    .load(imageUrl + post.attachment.url)
                    .timeout(30_000)
                    .into(descriptionImage)

                binding.descriptionImage.visibility = View.VISIBLE
            } else {
                binding.descriptionImage.visibility = View.GONE
            }

            binding.descriptionImage.setOnClickListener {
                onInteractionListener.onImagePreview(post)
            }

            if (post.likedByMe) {
                binding.Likes.isCheckable = true
            }

            Likes.isChecked = post.likedByMe
            Likes.text = post.likes.toString()

            Likes.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            Reposts.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            menu.isVisible = post.ownedByMe

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            Likes.text = calculateNumber(post.likes)
            Reposts.text = calculateNumber(post.reposts)
        }
    }
}

private class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}