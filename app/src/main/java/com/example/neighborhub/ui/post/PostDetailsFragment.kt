package com.example.neighborhub.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.neighborhub.R
import com.example.neighborhub.databinding.FragmentPostDetailsBinding
import com.example.neighborhub.ui.viewmodel.PostDetailsViewModel

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

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(PostDetailsViewModel::class.java)

        val postId = arguments?.let { PostDetailsFragmentArgs.fromBundle(it).postId }
        if (postId != null) {
            viewModel.getPostById(postId)
        } else {
            binding.contentText.text = "Post not found" // Provide fallback UI
        }

        viewModel.post.observe(viewLifecycleOwner) { post ->
            if (post != null) {
                binding.headlineText.text = post.headline
                binding.contentText.text = post.content
                binding.userNameText.text = post.userName

                // Load user profile image
                Glide.with(this)
                    .load(post.userPhotoUrl)
                    .placeholder(R.drawable.default_profile) // Replace with your placeholder image
                    .circleCrop()
                    .into(binding.userPhotoDetails)
            } else {
                binding.contentText.text = "Post not found"
            }
        }
    }
}