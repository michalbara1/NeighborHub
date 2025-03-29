package com.example.neighborhub.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.neighborhub.databinding.FragmentPostListBinding
import com.example.neighborhub.repository.PostRepository
import com.example.neighborhub.ui.adapters.PostsAdapter
import com.example.neighborhub.ui.viewmodel.PostViewModel
import com.example.neighborhub.ui.viewmodel.PostViewModelFactory
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController

class PostListFragment : Fragment() {

    private var _binding: FragmentPostListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PostViewModel
    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val repository = PostRepository(requireContext())
        viewModel = ViewModelProvider(this, PostViewModelFactory(repository)).get(PostViewModel::class.java)


        adapter = PostsAdapter(
            onPostClick = { postId ->
                val action = PostListFragmentDirections.actionPostListFragmentToPostDetailsFragment(postId)
                findNavController().navigate(action)
            }
        )


        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())


        setupSearch()


        viewModel.filteredPosts.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
        }


        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }


        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                binding.errorText.text = it
                binding.errorText.visibility = View.VISIBLE
            } ?: run {
                binding.errorText.visibility = View.GONE
            }
        }


        viewModel.fetchPosts()
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchPosts(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchPosts(it) }
                return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}