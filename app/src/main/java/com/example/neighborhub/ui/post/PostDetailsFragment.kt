package com.example.neighborhub.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.neighborhub.databinding.FragmentPostDetailsBinding
import com.example.neighborhub.ui.viewmodel.PostDetailsViewModel
import com.example.neighborhub.ui.post.PostDetailsFragmentArgs

class PostDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPostDetailsBinding
    private lateinit var viewModel: PostDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[PostDetailsViewModel::class.java]

        val headline = arguments?.let { PostDetailsFragmentArgs.fromBundle(it).headline }
        if (headline != null) {
            viewModel.getPostByHeadline(headline)
        } else {
            binding.contentText.text = "Post not found" // Provide fallback UI
        }


        viewModel.post.observe(viewLifecycleOwner) { post ->
            if (post != null) {
                binding.headlineText.text = post.headline
                binding.contentText.text = post.content
                binding.userNameText.text = post.userName
            } else {
                binding.contentText.text = "Post not found"
            }
        }
    }
}
