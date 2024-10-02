package ru.netology.nmedia

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.CalculateValues.calculateNumber
import ru.netology.nmedia.dto.Post

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            published = "21 мая в 18:36",
            likedByMe = false
        )

        with(binding){
            author.text = post.author
            content.text = post.content
            published.text = post.published

            CountOfLikes.text = post.likes.toString()
            CountOfReposts.text = post.reposts.toString()

            Likes.setOnClickListener {
                post.likedByMe = !post.likedByMe
                Likes.setImageResource(
                    if (post.likedByMe) {
                        post.likes++
                        CountOfLikes.text = calculateNumber(post.likes)
                        R.drawable.ic_baseline_favorite_24
                    } else {
                        post.likes--
                        CountOfLikes.text = calculateNumber(post.likes)
                        R.drawable.ic_baseline_favorite_border_24
                    }
                )
            }
            Reposts.setOnClickListener {
                post.reposts++
                CountOfReposts.text = calculateNumber(post.reposts)
            }
        }
    }
}