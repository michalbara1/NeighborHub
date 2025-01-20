package com.example.neighborhub.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.neighborhub.R
import com.example.neighborhub.model.Post
import com.example.neighborhub.ui.adapters.PostsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class ProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var nameText: TextView
    private lateinit var editProfileButton: Button
    private lateinit var logoutButton: Button
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postAdapter: PostsAdapter
    private val postList = mutableListOf<Post>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileImage = view.findViewById(R.id.image_profile)
        nameText = view.findViewById(R.id.name_text)
        editProfileButton = view.findViewById(R.id.edit_profile_btn)
        logoutButton = view.findViewById(R.id.logout_btn)
        postsRecyclerView = view.findViewById(R.id.posts_recycler_view)

        // Set up RecyclerView
        postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostsAdapter(postList)
        postsRecyclerView.adapter = postAdapter

        // Load posts from Firebase
        loadPostsFromFirebase()

        editProfileButton.setOnClickListener {
            // Handle edit profile action
        }

        logoutButton.setOnClickListener {
            // Handle logout action
        }

        return view
    }

    private fun loadPostsFromFirebase() {
        db.collection("posts")
            .get()
            .addOnSuccessListener { documents: QuerySnapshot ->
                postList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    postList.add(post)
                }
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle any errors here
            }
    }
}
