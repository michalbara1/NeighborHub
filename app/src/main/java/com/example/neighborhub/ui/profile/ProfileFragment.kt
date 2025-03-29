package com.example.neighborhub.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.neighborhub.R
import com.example.neighborhub.databinding.FragmentProfileBinding
import com.example.neighborhub.ui.viewmodel.ProfileViewModel
import com.example.neighborhub.ui.viewmodel.ProfileViewModelFactory
import com.example.neighborhub.repository.AuthRepository
import com.example.neighborhub.ui.adapters.UserPostsAdapter

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(requireActivity().application, AuthRepository())
    }

    private lateinit var userPostsAdapter: UserPostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        viewModel.fetchUserDetails()
        viewModel.fetchUserPosts()
        viewModel.verifyPostIds()
        setupRecyclerView()

        binding.logoutBtn.setOnClickListener {
            viewModel.logoutUser()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
        binding.editProfileBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
    }


    private fun setupRecyclerView() {
        userPostsAdapter = UserPostsAdapter(
            onEditClick = { post ->
                Log.d("ProfileFragment", "Editing post with ID: ${post.id}")


                val action = ProfileFragmentDirections.actionProfileFragmentToEditPostFragment(post.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { post ->
                Log.d("ProfileFragment", "Attempting to delete post with ID: ${post.id}")

                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Post")
                    .setMessage("Are you sure you want to delete this post?")
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.deletePost(post.id)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        )

        binding.postsRecyclerView.adapter = userPostsAdapter
        binding.postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.userPosts.observe(viewLifecycleOwner, Observer { posts ->
            userPostsAdapter.submitList(posts)
        })
    }


    private fun observeViewModel() {
        viewModel.userName.observe(viewLifecycleOwner, Observer { userName ->
            binding.userNameTextView.text = userName
        })

        viewModel.userEmail.observe(viewLifecycleOwner, Observer { userEmail ->
            binding.userEmailTextView.text = userEmail
        })

        viewModel.userPhotoUrl.observe(viewLifecycleOwner, Observer { photoUrl ->
            if (!photoUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(photoUrl)
                    .into(binding.imageProfile)
            }
        })

        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}