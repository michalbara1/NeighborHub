package com.example.neighborhub.ui.post

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.neighborhub.R
import com.example.neighborhub.databinding.FragmentAddPostBinding
import com.example.neighborhub.model.Post
import com.example.neighborhub.ui.viewmodel.AddPostViewModel
import com.example.neighborhub.ui.viewmodel.AddPostViewModelFactory
import java.util.UUID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddPostFragment : Fragment() {

    private lateinit var binding: FragmentAddPostBinding
    private val viewModel: AddPostViewModel by viewModels {
        AddPostViewModelFactory(requireContext())
    }

    private var imageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                binding.postImageView.visibility = View.VISIBLE
                Glide.with(requireContext()).load(it).into(binding.postImageView)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the click listener for the profile image to navigate to profile page
        binding.imageProfile.setOnClickListener {
            findNavController().navigate(R.id.action_addPostFragment_to_profileFragment)
        }

        binding.uploadImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Fetch and display current user info
        fetchCurrentUserInfo()

        // Observe the ViewModel
        observeViewModel()

        // Submit button click listener
        binding.submitButton.setOnClickListener {
            Log.d("AddPostFragment", "Submit button clicked")
            val headline = binding.headlineInput.text.toString()
            val content = binding.contentInput.text.toString()

            if (headline.isBlank() || content.isBlank()) {
                Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Get current user ID
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId != null) {
                    // Fetch user data from Firestore
                    FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                // Get user data
                                val username = document.getString("username") ?: "Unknown User"
                                val profileImageUrl = document.getString("profilePictureUrl") ?: ""

                                Log.d(
                                    "AddPostFragment",
                                    "Creating post with username: $username and profile image: $profileImageUrl"
                                )

                                // Create post with actual user data
                                val post = Post(
                                    id = UUID.randomUUID().toString(),
                                    headline = headline,
                                    content = content,
                                    userName = username,
                                    userPhotoUrl = profileImageUrl,
                                    userId = currentUserId
                                )

                                // Handle image upload and post creation
                                if (imageUri != null) {
                                    Log.d("AddPostFragment", "Uploading image")
                                    viewModel.uploadImage(imageUri!!) { imageUrl ->
                                        if (imageUrl != null) {
                                            Log.d(
                                                "AddPostFragment",
                                                "Image uploaded successfully: $imageUrl"
                                            )
                                            post.imageUrl = imageUrl
                                            viewModel.addPost(post)
                                        } else {
                                            Log.e("AddPostFragment", "Failed to upload image")
                                            Toast.makeText(
                                                requireContext(),
                                                "Failed to upload image.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    Log.d("AddPostFragment", "Adding post without image")
                                    viewModel.addPost(post)
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "User profile not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("AddPostFragment", "Failed to get user data", e)
                            Toast.makeText(
                                requireContext(),
                                "Error: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "You need to be logged in to post",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun fetchCurrentUserInfo() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get user data
                        val username = document.getString("username") ?: "Unknown User"
                        val profileImageUrl = document.getString("profilePictureUrl") ?: ""

                        // Update UI with user data
                        binding.userNameTextView.text = username

                        // Load profile image
                        if (profileImageUrl.isNotEmpty()) {
                            Glide.with(requireContext())
                                .load(profileImageUrl)
                                .circleCrop()
                                .into(binding.imageProfile)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AddPostFragment", "Failed to get user data", e)
                }
        }
    }

    private fun observeViewModel() {
        viewModel.success.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Post added successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}