package com.example.neighborhub.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.neighborhub.databinding.FragmentProfileBinding
import com.example.neighborhub.repository.AuthRepository
import com.example.neighborhub.repository.PostRepository
import com.example.neighborhub.ui.adapters.PostsAdapter
import com.example.neighborhub.ui.viewmodel.ProfileViewModel
import com.example.neighborhub.ui.viewmodel.ProfileViewModelFactory

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(AuthRepository(), PostRepository(requireContext()))
    }

    private lateinit var postAdapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        viewModel.fetchUserDetails()
    }

    private fun setupRecyclerView() {
        postAdapter = PostsAdapter { post ->
            // Handle post click (e.g., navigate to edit post screen)
        }
        binding.postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.userName.observe(viewLifecycleOwner, Observer { userName ->
            binding.userNameTextView.text = userName
        })

        viewModel.userPosts.observe(viewLifecycleOwner, Observer { posts ->
            postAdapter.submitList(posts)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                // Show error message to the user
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}