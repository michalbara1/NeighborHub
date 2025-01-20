package com.example.neighborhub.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.neighborhub.R
import com.example.neighborhub.model.Post
import com.example.neighborhub.ui.adapters.PostsAdapter

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var postsAdapter: PostsAdapter
    private lateinit var userNameTextView: TextView
    private lateinit var profileEditTextView: TextView
    private lateinit var signOutButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var drawButton: ImageButton
    private lateinit var eraseButton: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        initializeViews(view)
        setupClickListeners()
        setupRecyclerView(view)

        // Set initial data
        userNameTextView.text = "Username" // Replace with actual username
    }

    private fun initializeViews(view: View) {
        userNameTextView = view.findViewById(R.id.user_name_text)
        profileEditTextView = view.findViewById(R.id.tvProfileEdit)
        signOutButton = view.findViewById(R.id.signout_btn)
        profileImageView = view.findViewById(R.id.ivProfile)
        drawButton = view.findViewById(R.id.btnDraw)
        eraseButton = view.findViewById(R.id.btnErase)
    }

    private fun setupClickListeners() {
        signOutButton.setOnClickListener {
            // Handle sign out logic
            // You might want to add your authentication logic here
        }

        profileEditTextView.setOnClickListener {
            // Handle profile edit click
        }

        drawButton.setOnClickListener {
            // Handle draw tool click
        }

        eraseButton.setOnClickListener {
            // Handle erase tool click
        }
    }

    private fun setupRecyclerView(view: View) {
        // Note: You'll need to add a RecyclerView to your layout
        // val recyclerView = view.findViewById<RecyclerView>(R.id.rvPosts)
        // postsAdapter = PostsAdapter()
        // recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // recyclerView.adapter = postsAdapter
        // postsAdapter.submitList(getUserPosts())
    }


}

