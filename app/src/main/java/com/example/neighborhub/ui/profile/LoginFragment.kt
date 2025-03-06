package com.example.neighborhub.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.neighborhub.R
import com.example.neighborhub.databinding.FragmentLoginBinding
import com.example.neighborhub.ui.viewmodel.UserViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val userViewModel: UserViewModel by viewModels()

    // ViewBinding for the Login Fragment UI
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text?.toString()?.trim() ?: ""
            val password = binding.passwordEditText.text?.toString()?.trim() ?: ""

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rememberMe = binding.rememberMeCheckbox.isChecked
            loginUser(email, password, rememberMe)
        }

        // Automatically navigate if a user is already logged in
        val currentUser = userViewModel.getCurrentUser()
        if (currentUser != null) {
            Toast.makeText(requireContext(), "User already logged in: ${currentUser.email}", Toast.LENGTH_SHORT).show()
            navigateToMainScreen()
        } else {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks by cleaning up the binding reference
        _binding = null
    }

    private fun loginUser(email: String, password: String, rememberMe: Boolean = false) {
        userViewModel.login(email, password).observe(viewLifecycleOwner) { result ->
            when (result) {
                is UserViewModel.LoginResult.Success -> {
                    if (rememberMe) {
                        saveUserCredentials(email, password)
                    }
                    navigateToMainScreen()
                    Toast.makeText(requireContext(), "Login successful, welcome!", Toast.LENGTH_SHORT).show()
                }
                is UserViewModel.LoginResult.Failure -> {
                    Toast.makeText(requireContext(), "Login failed: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToMainScreen() {
        // Implement navigation to the main screen
        // For example, using Navigation Component:
        // findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        findNavController().navigate(R.id.action_loginFragment_to_postListFragment)
    }

    private fun saveUserCredentials(email: String, password: String) {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }
}