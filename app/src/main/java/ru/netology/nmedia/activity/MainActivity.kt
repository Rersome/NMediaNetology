package ru.netology.nmedia.activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()
        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
                viewModel.shareById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            val newEditingLauncher = registerForActivityResult(NewPostResultContract()) { result ->
                result ?: return@registerForActivityResult
                viewModel.applyChangeAndSave(result)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                newEditingLauncher.launch(post.content)
            }
        })
        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            val newPost = posts.size > adapter.currentList.size && adapter.currentList.isNotEmpty()
            adapter.submitList(posts) {
                if (newPost) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }

//        viewModel.edited.observe(this) {
//            if (it.id != 0L) {
//                binding.originalText.setText(it.content)
//                binding.content.setText(it.content)
//                binding.content.focusAndShowKeyboard()
//            }
//        }
//
//        binding.save.setOnClickListener {
//            val text = binding.content.text.toString()
//            if (text.isBlank()) {
//                Toast.makeText(this@MainActivity, R.string.error_empty_content, Toast.LENGTH_SHORT)
//                    .show()
//                return@setOnClickListener
//            }
//            viewModel.applyChangeAndSave(text)
//
//            binding.content.setText("")
//            binding.content.clearFocus()
//            AndroidUtils.hideKeyboard(it)
//        }
//
//        binding.imageOfCancel.setOnClickListener {
//            binding.content.setText("")
//            binding.content.clearFocus()
//            AndroidUtils.hideKeyboard(it)
//            viewModel.cancelEdit()
//        }

        val newPostLauncher = registerForActivityResult(NewPostResultContract()) { result ->
            result ?: return@registerForActivityResult
            viewModel.applyChangeAndSave(result)
        }

        binding.fab.setOnClickListener {
            newPostLauncher.launch(null)
        }
    }
}