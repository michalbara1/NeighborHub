package com.example.neighborhub.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.neighborhub.databinding.ItemUserPostBinding

import com.example.neighborhub.model.Post

class UserPostsAdapter(
    private val onEditClick: (Post) -> Unit,
    private val onDeleteClick: (Post) -> Unit
) : ListAdapter<Post, UserPostsAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemUserPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PostViewHolder(private val binding: ItemUserPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.postTitle.text = post.headline
            binding.postContent.text = post.content

            // Load post image if available
            if (!post.imageUrl.isNullOrEmpty()) {
                binding.postImage.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(post.imageUrl)
                    .into(binding.postImage)
            } else {
                binding.postImage.visibility = View.GONE
            }

            // Setup edit and delete buttons
            binding.editButton.setOnClickListener { onEditClick(post) }
            binding.deleteButton.setOnClickListener { onDeleteClick(post) }
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