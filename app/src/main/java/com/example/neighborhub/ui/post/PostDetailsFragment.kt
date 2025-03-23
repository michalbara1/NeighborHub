package com.example.neighborhub.ui.post

import android.os.Bundle
import android.util.Log
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
    private val TAG = "PostDetailsFragment"
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
        Log.d(TAG, "Loading post with ID: $postId")

        if (postId != null) {
            viewModel.getPostById(postId)
        } else {
            binding.contentText.text = "Post not found" // Provide fallback UI
            Log.e(TAG, "No post ID provided in arguments")
        }

        viewModel.post.observe(viewLifecycleOwner) { post ->
            if (post != null) {
                Log.d(TAG, "Loaded post: ${post.id}")
                Log.d(TAG, "Emoji data: unicode=${post.emojiUnicode}, name=${post.emojiName}")

                binding.headlineText.text = post.headline
                binding.contentText.text = post.content
                binding.userNameText.text = post.userName

                // Load user profile image
                Glide.with(this)
                    .load(post.userPhotoUrl)
                    .placeholder(R.drawable.default_profile)
                    .circleCrop()
                    .into(binding.userPhotoDetails)

                // Display emoji if available
                if (!post.emojiUnicode.isNullOrEmpty()) {
                    Log.d(TAG, "Showing emoji with unicode: ${post.emojiUnicode}")
                    try {
                        val emoji = convertUnicodeToEmoji(post.emojiUnicode!!)
                        Log.d(TAG, "Converted emoji: $emoji")
                        binding.postEmojiTextView.text = emoji
                        binding.postEmojiTextView.visibility = View.VISIBLE

                        // Increase text size for visibility
                        binding.postEmojiTextView.textSize = 32f

                        // Add a temporary background for debugging
                        binding.postEmojiTextView.setBackgroundResource(android.R.color.holo_blue_light)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error displaying emoji", e)
                        binding.postEmojiTextView.text = "😊" // Fallback emoji
                        binding.postEmojiTextView.visibility = View.VISIBLE
                    }
                } else {
                    Log.d(TAG, "No emoji data for this post")
                    binding.postEmojiTextView.visibility = View.GONE
                }
            } else {
                Log.e(TAG, "Post not found or failed to load")
                binding.contentText.text = "Post not found"
            }
        }
    }

    private fun convertUnicodeToEmoji(unicodeStr: String): String {
        if (unicodeStr.isEmpty()) {
            Log.d(TAG, "Empty unicode string provided")
            return ""
        }

        return try {
            Log.d(TAG, "Converting unicode: $unicodeStr")
            val processedString = unicodeStr.replace("U+", "")
            Log.d(TAG, "After U+ replacement: $processedString")

            val parts = processedString.split(" ")
            Log.d(TAG, "Split into ${parts.size} parts: $parts")

            val filteredParts = parts.filter { it.isNotEmpty() }
            Log.d(TAG, "After filtering: $filteredParts")

            val codePoints = filteredParts.map {
                try {
                    val codePoint = Integer.parseInt(it, 16)
                    Log.d(TAG, "Parsed hex $it to int: $codePoint")
                    codePoint
                } catch (e: NumberFormatException) {
                    Log.e(TAG, "Failed to parse hex: $it", e)
                    0x1F60A // Fallback to smiling face emoji
                }
            }

            val chars = codePoints.map {
                try {
                    val charArray = Character.toChars(it)
                    Log.d(TAG, "Converted codepoint $it to char array of length ${charArray.size}")
                    charArray
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to convert codepoint to char: $it", e)
                    Character.toChars(0x1F60A) // Fallback to smiling face emoji
                }
            }

            val result = chars.joinToString("") { it.concatToString() }
            Log.d(TAG, "Final emoji result: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error in emoji conversion process", e)
            "😊" // Fallback emoji
        }
    }
}