package com.example.neighborhub.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.neighborhub.R
import com.example.neighborhub.databinding.FragmentRegisterBinding
import com.example.neighborhub.ui.viewmodel.UserViewModel

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()
    private var profilePictureUrl: String? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // Delete the previously uploaded image if needed
            profilePictureUrl?.let { previousUrl ->
                userViewModel.deleteProfileImage(previousUrl) { success ->
                    requireActivity().runOnUiThread {
                        if (success) {
                            Toast.makeText(requireContext(), "Previous image deleted.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete previous image.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            // Upload the new image
            userViewModel.uploadProfileImage(it).observe(viewLifecycleOwner) { status ->
                val (success, imageUrl) = status
                requireActivity().runOnUiThread {
                    if (success) {
                        profilePictureUrl = imageUrl
                        binding.profilePicturePreview.visibility = View.VISIBLE
                        Glide.with(this)
                            .load(imageUrl)
                            .into(binding.profilePicturePreview)
                        Toast.makeText(requireContext(), "Profile picture uploaded!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Upload failed: $imageUrl", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener { registerUser() }
        binding.uploadPictureButton.setOnClickListener { imagePicker.launch("image/*") }
        binding.goBackButton.setOnClickListener { handleGoBack() }
    }

    private fun registerUser() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val username = binding.usernameEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "All fields are required.", Toast.LENGTH_SHORT).show()
            return
        }

        userViewModel.register(email, password, username).observe(viewLifecycleOwner) { result ->
            when (result) {
                is UserViewModel.RegistrationResult.Success -> {
                    val userId = userViewModel.getCurrentUser()?.uid
                    userId?.let {
                        val updatedData = HashMap<String, Any>()
                        updatedData["profilePictureUrl"] = profilePictureUrl ?: ""
                        userViewModel.updateUserDetails(it, updatedData) { success: Boolean ->
                            if (success) {
                                Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Failed to update profile picture.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.registerFragment)
                }
                is UserViewModel.RegistrationResult.Failure -> {
                    Toast.makeText(requireContext(), "Registration failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleGoBack() {
        profilePictureUrl?.let { imageUrl ->
            userViewModel.deleteProfileImage(imageUrl) { success ->
                requireActivity().runOnUiThread {
                    if (success) {
                        Toast.makeText(requireContext(), "Unused profile picture deleted.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete unused image.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}