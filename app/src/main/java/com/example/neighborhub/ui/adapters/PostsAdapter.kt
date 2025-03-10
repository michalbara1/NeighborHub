package com.example.neighborhub.ui.adapters

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

class PostsAdapter(private val onPostClick: (Post) -> Unit) :
    ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

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
            // Bind post content
            binding.postTitle.text = post.headline
            binding.postContent.text = post.content

            // Bind user information
            binding.userName.text = post.userName
            binding.userHeadline.text = post.headline

            // Load user profile image
            Glide.with(binding.root.context)
                .load(post.userPhotoUrl)
                .placeholder(R.drawable.default_profile)
                .circleCrop()
                .into(binding.userProfileImage)

            // Load post image if available
            if (!post.imageUrl.isNullOrEmpty()) {
                binding.postImage.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(post.imageUrl)
                    .centerCrop()
                    .into(binding.postImage)
            } else {
                binding.postImage.visibility = View.GONE
            }

            // Set click listener
            binding.root.setOnClickListener { onPostClick(post) }
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