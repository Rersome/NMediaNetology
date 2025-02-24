import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ru.netology.nmedia.databinding.FragmentImagePreviewBinding

class ImagePreviewFragment : Fragment() {
    private lateinit var binding: FragmentImagePreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImagePreviewBinding.inflate(inflater, container, false)

        val imageUrl = arguments?.getString("IMAGE_URL")

        Glide.with(this)
            .load(imageUrl)
            .into(binding.fullscreenImage)

        return binding.root
    }
}