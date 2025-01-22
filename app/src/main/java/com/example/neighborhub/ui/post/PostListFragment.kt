package com.example.neighborhub.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.neighborhub.databinding.FragmentPostListBinding
import com.example.neighborhub.ui.adapters.PostsAdapter
import com.example.neighborhub.ui.viewmodel.PostViewModel

class PostListFragment : Fragment() {

    private lateinit var binding: FragmentPostListBinding
    private lateinit var viewModel: PostViewModel
    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[PostViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
        viewModel.fetchPosts()

        binding.searchBar.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchPosts(newText.orEmpty()) // Perform the search on each keystroke
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = PostsAdapter { post ->
            val action = PostListFragmentDirections
                .actionPostListFragmentToPostDetailsFragment(post.headline)
            findNavController().navigate(action)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts ?: emptyList())
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            binding.errorText.text = error
            binding.errorText.visibility = if (error.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}