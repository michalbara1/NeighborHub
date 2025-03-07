package com.example.neighborhub.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.neighborhub.R
import com.example.neighborhub.databinding.FragmentProfileBinding
import com.example.neighborhub.ui.viewmodel.ProfileViewModel
import com.example.neighborhub.ui.viewmodel.ProfileViewModelFactory
import com.example.neighborhub.repository.AuthRepository

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(AuthRepository())
    }

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

        binding.logoutBtn.setOnClickListener {
            viewModel.logoutUser()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
        binding.editProfileBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
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