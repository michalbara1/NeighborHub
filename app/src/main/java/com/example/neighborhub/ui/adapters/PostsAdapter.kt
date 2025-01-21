//package com.example.neighborhub.ui.adapters
//
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.example.neighborhub.databinding.ListItemPostBinding
//import com.example.neighborhub.model.Post
//
//class PostsAdapter : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
//        val binding = ListItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return PostViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
//        val post = getItem(position)
//        holder.bind(post)
//    }
//
//    class PostViewHolder(private val binding: ListItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(post: Post) {
//            binding.post = post // Make sure this matches the layout
//            binding.executePendingBindings() // Ensures binding is applied immediately
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
//

package com.example.neighborhub.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.example.neighborhub.R
import com.example.neighborhub.model.Post

class PostsAdapter(private val postList: MutableList<Post>) :
    RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.user_photo)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val postHeadline: TextView = itemView.findViewById(R.id.post_headline)
        val postContent: TextView = itemView.findViewById(R.id.post_content)
        val editButton: Button = itemView.findViewById(R.id.btn_edit_post)
        val deleteButton: Button = itemView.findViewById(R.id.btn_delete_post)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.userName.text = post.userName
        holder.postHeadline.text = post.headline
        holder.postContent.text = post.content

//        Glide.with(holder.itemView.context)
//            .load(post.userPhotoUrl)
//            .placeholder(R.drawable.placeholder_image)
//            .into(holder.userImage)

        holder.editButton.setOnClickListener {
            // Handle edit action
        }

        holder.deleteButton.setOnClickListener {
            postList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, postList.size)
        }
    }

    override fun getItemCount(): Int = postList.size
}
