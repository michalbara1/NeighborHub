package com.example.neighborhub.ui.post

import android.app.Activity
import android.content.Intent
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
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.neighborhub.databinding.FragmentEditPostBinding
import com.example.neighborhub.model.Post
import com.example.neighborhub.model.data.PostDao
import com.example.neighborhub.model.data.AppDatabase
import com.example.neighborhub.repository.AuthRepository
import com.example.neighborhub.ui.viewmodel.ProfileViewModel
import com.example.neighborhub.ui.viewmodel.ProfileViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class EditPostFragment : Fragment() {

    private var _binding: FragmentEditPostBinding? = null
    private val binding get() = _binding!!

    private var postId: String? = null
    private var imageUri: Uri? = null
    private var originalImageUrl: String? = null
    private lateinit var postDao: PostDao

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(requireActivity().application, AuthRepository())
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.postImageView.setImageURI(imageUri)
            binding.postImageView.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPostBinding.inflate(inflater, container, false)
        postDao = AppDatabase.getInstance(requireContext()).postDao()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postId = arguments?.getString("postId")

        if (postId == null) {
            Toast.makeText(context, "Post ID not found", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        loadPostData()

        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        binding.btnSavePost.setOnClickListener {
            updatePost()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadPostData() {
        binding.progressBar.visibility = View.VISIBLE

        val postIdValue = postId
        if (postIdValue == null || postIdValue.isEmpty()) {
            Toast.makeText(context, "Invalid Post ID", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        Log.d("EditPostFragment", "Trying to load post with ID: $postIdValue")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val document = FirebaseFirestore.getInstance()
                    .collection("posts")
                    .document(postIdValue)
                    .get()
                    .await()

                withContext(Dispatchers.Main) {
                    if (document.exists()) {
                        val post = document.toObject(Post::class.java)
                        if (post != null) {
                            post.id = postIdValue
                            displayPostData(post)
                            Log.d("EditPostFragment", "Post loaded successfully: ${post.headline}")
                        } else {
                            Toast.makeText(context, "Failed to parse post data", Toast.LENGTH_SHORT).show()
                            Log.e("EditPostFragment", "Failed to parse post data for ID: $postIdValue")
                            findNavController().navigateUp()
                        }
                    } else {
                        Toast.makeText(context, "Post not found", Toast.LENGTH_SHORT).show()
                        Log.e("EditPostFragment", "Post not found for ID: $postIdValue")
                        findNavController().navigateUp()
                    }
                    binding.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error loading post: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditPostFragment", "Error loading post with ID: $postIdValue", e)
                    binding.progressBar.visibility = View.GONE
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun displayPostData(post: Post?) {
        post?.let {
            binding.etHeadline.setText(it.headline)
            binding.etContent.setText(it.content)
            originalImageUrl = it.imageUrl

            if (!it.imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(it.imageUrl)
                    .into(binding.postImageView)
                binding.postImageView.visibility = View.VISIBLE
            }
        }
        binding.progressBar.visibility = View.GONE
    }

    private fun updatePost() {
        binding.progressBar.visibility = View.VISIBLE

        val headline = binding.etHeadline.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (headline.isEmpty() || content.isEmpty()) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            return
        }

        if (imageUri != null) {
            uploadImageToCloudinary()
        } else {
            updatePostData(originalImageUrl)
        }
    }

    private fun uploadImageToCloudinary() {
        val imageUri = imageUri ?: run {
            Toast.makeText(context, "Image URI is null", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            return
        }

        MediaManager.get().upload(imageUri)
            .option("public_id", "post_images/${UUID.randomUUID()}")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                    val imageUrl = resultData?.get("secure_url") as? String
                    updatePostData(imageUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    activity?.runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, "Image upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun updatePostData(imageUrl: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val postId = postId ?: throw IllegalArgumentException("Post ID is null")
                val firestore = FirebaseFirestore.getInstance()


                val existingPostSnapshot = firestore.collection("posts").document(postId).get().await()
                val existingPost = existingPostSnapshot.toObject(Post::class.java)

                if (existingPost == null) {
                    throw IllegalArgumentException("Post not found")
                }


                val currentUser = viewModel.getCurrentUser()
                val userName = currentUser?.displayName ?: existingPost.userName
                val userPhotoUrl = currentUser?.photoUrl?.toString() ?: existingPost.userPhotoUrl


                val updatedPost = existingPost.copy(
                    headline = binding.etHeadline.text.toString().trim(),
                    content = binding.etContent.text.toString().trim(),
                    imageUrl = imageUrl ?: existingPost.imageUrl,
                    userName = userName,
                    userPhotoUrl = userPhotoUrl,
                    lastUpdated = System.currentTimeMillis()
                )

                firestore.collection("posts").document(postId)
                    .set(updatedPost, com.google.firebase.firestore.SetOptions.merge())
                    .await()


                postDao.updatePost(updatedPost)

                withContext(Dispatchers.Main) {
                    viewModel.fetchUserPosts() // Refresh the posts
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error updating post: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditPostFragment", "Error updating post", e)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}