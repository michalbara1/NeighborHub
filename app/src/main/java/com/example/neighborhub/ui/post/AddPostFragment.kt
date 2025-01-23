package com.example.neighborhub.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.neighborhub.databinding.FragmentAddPostBinding
import com.example.neighborhub.model.Post
import com.example.neighborhub.ui.viewmodel.AddPostViewModel

class AddPostFragment : Fragment() {

    private lateinit var binding: FragmentAddPostBinding
    private lateinit var viewModel: AddPostViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[AddPostViewModel::class.java]

        binding.submitButton.setOnClickListener {
            val headline = binding.headlineInput.text.toString()
            val content = binding.contentInput.text.toString()

            if (headline.isBlank() || content.isBlank()) {
                Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val post = Post(
                    id = null, // Firebase will generate the ID
                    headline = headline,
                    content = content,
                    userName = "Current User", // Replace with the current user's name
                    userPhotoUrl = "https://example.com/photo.jpg" // Replace with the current user's photo URL
                )

                viewModel.addPost(post)
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.success.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Post added successfully!", Toast.LENGTH_SHORT)
                    .show()
                requireActivity().onBackPressed()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
