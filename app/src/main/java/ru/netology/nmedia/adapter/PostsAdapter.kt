package ru.netology.nmedia.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.CalculateValues.calculateNumber
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
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    var list = emptyList<Post>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)

        holder.itemView.setOnClickListener { onInteractionListener.onPostClick(post) }
    }
}

class PostViewHolder(
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

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
}