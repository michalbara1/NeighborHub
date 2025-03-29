package com.example.neighborhub.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.neighborhub.R
import com.example.neighborhub.databinding.ItemPostBinding
import com.example.neighborhub.model.Post

class PostsAdapter(
    private val onPostClick: (String) -> Unit,
    private val showLocationInfo: Boolean = false,
    private val onMapButtonClick: ((Post) -> Unit)? = null
) : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {

            binding.postTitle.text = post.headline
            binding.postContent.text = post.content


            binding.userName.text = post.userName
            binding.userHeadline.text = post.headline


            Glide.with(binding.root.context)
                .load(post.userPhotoUrl)
                .placeholder(R.drawable.default_profile)
                .circleCrop()
                .into(binding.userProfileImage)


            if (!post.imageUrl.isNullOrEmpty()) {
                binding.postImage.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(post.imageUrl)
                    .centerCrop()
                    .into(binding.postImage)
            } else {
                binding.postImage.visibility = View.GONE
            }


            if (!post.emojiUnicode.isNullOrEmpty()) {
                val emoji = convertUnicodeToEmoji(post.emojiUnicode!!)
                binding.postEmoji.text = emoji
                binding.postEmoji.visibility = View.VISIBLE
            } else {
                binding.postEmoji.visibility = View.GONE
            }


            if (showLocationInfo && post.latitude != null && post.longitude != null) {
                binding.locationInfoLayout.visibility = View.VISIBLE
                binding.locationText.text = "Location: ${String.format("%.6f, %.6f", post.latitude, post.longitude)}"


                binding.viewOnMapButton.apply {
                    visibility = if (onMapButtonClick != null) View.VISIBLE else View.GONE
                    setOnClickListener { onMapButtonClick?.invoke(post) }
                }
            } else {
                binding.locationInfoLayout.visibility = View.GONE
            }


            binding.root.setOnClickListener { onPostClick(post.id) }
        }

        private fun convertUnicodeToEmoji(unicodeStr: String): String {
            if (unicodeStr.isEmpty()) return ""

            return try {
                unicodeStr.replace("U+", "")
                    .split(" ")
                    .filter { it.isNotEmpty() }
                    .map {
                        try {
                            Integer.parseInt(it, 16)
                        } catch (e: NumberFormatException) {
                            Log.e("PostsAdapter", "Failed to parse: $it", e)
                            0
                        }
                    }
                    .map {
                        try {
                            Character.toChars(it)
                        } catch (e: Exception) {
                            Log.e("PostsAdapter", "Failed to convert: $it to char", e)
                            charArrayOf(' ')
                        }
                    }
                    .joinToString("") { it.concatToString() }
            } catch (e: Exception) {
                Log.e("PostsAdapter", "Error converting unicode to emoji", e)
                "😊"
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}