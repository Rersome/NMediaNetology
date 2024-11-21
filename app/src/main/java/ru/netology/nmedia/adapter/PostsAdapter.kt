package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentCardPostBinding
import ru.netology.nmedia.dto.CalculateValues.calculateNumber

import ru.netology.nmedia.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun startVideo(post: Post)
    fun onPostClick(post: Post)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
): ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    var list = emptyList<Post>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = FragmentCardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)

        holder.itemView.setOnClickListener { onInteractionListener.onPostClick(post) }
    }
}

class PostViewHolder(
    private val binding: FragmentCardPostBinding,
    private val onInteractionListener: OnInteractionListener,
    private val videoButton: ImageButton = binding.video
): RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post){
        binding.apply {
            author.text = post.author
            content.text = post.content
            published.text = post.published

            Likes.isChecked = post.likedByMe
            Likes.text = post.likes.toString()

            Reposts.text = post.reposts.toString()

            Likes.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            Reposts.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            if (post.video.isEmpty()) {
                videoButton.visibility = View.GONE
            } else {
                videoButton.visibility = View.VISIBLE
                videoButton.setOnClickListener {
                    onInteractionListener.startVideo(post)
                }
            }

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

class PostDiffCallback: DiffUtil.ItemCallback<Post>(){
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
}