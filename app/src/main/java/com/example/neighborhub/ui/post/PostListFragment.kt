//package com.example.neighborhub.ui.post
//
//import android.os.Bundle
//import android.view.*
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModelProvider
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.neighborhub.R
//import com.example.neighborhub.databinding.FragmentPostListBinding
//import com.example.neighborhub.ui.adapters.PostsAdapter
//import com.example.neighborhub.ui.viewmodel.PostViewModel
//
//class PostListFragment : Fragment() {
//
//    private lateinit var binding: FragmentPostListBinding
//    private lateinit var viewModel: PostViewModel
//    private lateinit var adapter: PostsAdapter
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentPostListBinding.inflate(inflater, container, false)
//        setHasOptionsMenu(true) // Enable options menu
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        viewModel = ViewModelProvider(this)[PostViewModel::class.java]
//
//        setupRecyclerView()
//        observeViewModel()
//        viewModel.fetchPosts()
//
//        binding.searchBar.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                viewModel.searchPosts(newText.orEmpty()) // Perform the search on each keystroke
//                return true
//            }
//        })
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_post_list, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_add_post -> {
//                navigateToAddPost()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//
//    private fun navigateToAddPost() {
//        findNavController().navigate(R.id.action_postListFragment_to_addPostFragment)
//    }
//
//    private fun setupRecyclerView() {
//        adapter = PostsAdapter { post ->
//            val action = PostListFragmentDirections
//                .actionPostListFragmentToPostDetailsFragment(post.headline)
//            findNavController().navigate(action)
//        }
//        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        binding.recyclerView.adapter = adapter
//    }
//
//    private fun observeViewModel() {
//        viewModel.posts.observe(viewLifecycleOwner) { posts ->
//            adapter.submitList(posts ?: emptyList())
//        }
//
//        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
//            binding.errorText.text = error
//            binding.errorText.visibility = if (error.isNullOrEmpty()) View.GONE else View.VISIBLE
//        }
//
//        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//        }
//    }
//}