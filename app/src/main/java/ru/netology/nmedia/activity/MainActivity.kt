package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.CalculateValues.calculateNumber
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel by viewModels<PostViewModel>()
        viewModel.data.observe(this) {post->
            with(binding) {
                author.text = post.author
                content.text = post.content
                published.text = post.published

                Likes.setImageResource(if (post.likedByMe) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24)

                CountOfLikes.text = calculateNumber(post.likes)
                CountOfReposts.text = calculateNumber(post.reposts)
            }
        }
        binding.Likes.setOnClickListener {
            viewModel.like()
        }
        binding.Reposts.setOnClickListener {
            viewModel.repost()
        }
    }
}