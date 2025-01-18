/*package com.example.neighborhub.ui.post

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.neighborhub.R
import com.example.neighborhub.ui.adapters.PostsAdapter
import com.example.neighborhub.ui.viewmodel.HomeViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var postsAdapter: PostsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        postsAdapter = PostsAdapter()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = postsAdapter

        homeViewModel.posts.observe(viewLifecycleOwner, Observer { posts ->
            postsAdapter.submitList(posts)
        })

        homeViewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        homeViewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        homeViewModel.fetchPosts()
    }
}*/
