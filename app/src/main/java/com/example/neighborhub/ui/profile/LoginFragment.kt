package com.example.neighborhub.ui.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.widget.Toast
import androidx.lifecycle.Lifecycle
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
        Log.d("LoginFragment", "onViewCreated called")

        binding.loginButton.setOnClickListener {
            Log.d("LoginFragment", "Login button clicked")
            val email = binding.emailEditText.text?.toString()?.trim() ?: ""
            val password = binding.passwordEditText.text?.toString()?.trim() ?: ""

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        val currentUser = userViewModel.getCurrentUser()
        if (currentUser != null) {
            Log.d("LoginFragment", "User already logged in: ${currentUser.email}")
            navigateToMainScreen()
        } else {
            Log.d("LoginFragment", "No user logged in")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks by cleaning up the binding reference
        _binding = null
    }

    private fun loginUser(email: String, password: String, rememberMe: Boolean = false) {
        if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            userViewModel.login(email, password).observe(viewLifecycleOwner) { result ->
                when (result) {
                    is UserViewModel.LoginResult.Success -> {
                        Log.d("LoginFragment", "Login successful, navigating to PostListFragment")
                        if (rememberMe) {
                            saveUserCredentials(email, password)
                        }
                        navigateToMainScreen()
                        Toast.makeText(requireContext(), "Login successful, welcome!", Toast.LENGTH_SHORT).show()
                    }
                    is UserViewModel.LoginResult.Failure -> {
                        Log.e("LoginFragment", "Login failed: ${result.message}")
                        Toast.makeText(requireContext(), "Login failed: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Log.e("LoginFragment", "Fragment is not in a valid state to perform login")
        }
    }

    private fun navigateToMainScreen() {
        Log.d("LoginFragment", "Attempting to navigate to PostListFragment")
        try {
            findNavController().navigate(R.id.action_loginFragment_to_postListFragment)
            Log.d("LoginFragment", "Navigation to PostListFragment successful")
        } catch (e: Exception) {
            Log.e("LoginFragment", "Navigation failed: ${e.message}")
        }
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