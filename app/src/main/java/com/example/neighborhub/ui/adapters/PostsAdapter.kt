//package com.example.neighborhub.ui.adapters
//
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.neighborhub.R
//import com.example.neighborhub.databinding.ItemPostBinding
//import com.example.neighborhub.model.Post
//import de.hdodenhof.circleimageview.CircleImageView
//
//class PostsAdapter(private val onClick: (Post) -> Unit) :
//    ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
//        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return PostViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
//        val post = getItem(position)
//        holder.bind(post) { clickedPost ->
//            if (!clickedPost.headline.isNullOrEmpty()) {
//                onClick(clickedPost)
//            } else {
//                Log.e("PostsAdapter", "Post headline is null or empty for post ID: ${clickedPost.id}")
//                Toast.makeText(
//                    holder.itemView.context,
//                    "Post cannot be opened because it has no headline.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }
//
//    class PostViewHolder(private val binding: ItemPostBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(post: Post, onClick: (Post) -> Unit) {
//            binding.postHeadline.text = post.headline
//            binding.postContent.text = post.content
//            binding.root.setOnClickListener { onClick(post) }
//
//            // Load user photo using Glide
//            Glide.with(binding.userPhoto.context)
//                .load(post.userPhotoUrl) // Assuming userPhotoUrl is a property in Post model
//                .placeholder(R.drawable.woman) // Placeholder image
//                .into(binding.userPhoto)
//        }
//    }
//
//    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
//        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
//            return oldItem.id == newItem.id
//        }
//
//        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
//            return oldItem == newItem
//        }
//    }
//}

package com.example.neighborhub.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.neighborhub.databinding.ItemPostBinding
import com.example.neighborhub.model.Post

class PostsAdapter(private val onClick: (Post) -> Unit) :
    ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post) { clickedPost ->
            if (!clickedPost.headline.isNullOrEmpty()) {
                onClick(clickedPost)
            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "Post cannot be opened because it has no headline.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post, onClick: (Post) -> Unit) {
            binding.post = post
            binding.root.setOnClickListener { onClick(post) }
            binding.executePendingBindings()
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}