package com.example.neighborhub.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import android.widget.Toast
import com.example.neighborhub.R
import com.example.neighborhub.databinding.FragmentLoginBinding
import com.example.neighborhub.ui.viewmodel.LoginViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val loginViewModel: LoginViewModel by viewModels()

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
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            // Validate input fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Attempt to login via ViewModel
            loginViewModel.loginUser(email, password).observe(viewLifecycleOwner, Observer { isSuccess ->
                if (isSuccess) {
                    // Login successful, show a toast message
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                } else {
                    // Login failed, show a toast message
                    Toast.makeText(requireContext(), "Login failed. Check your credentials", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Automatically navigate if a user is already logged in
        loginViewModel.getCurrentUser()?.let {
            Toast.makeText(requireContext(), "User already logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks by cleaning up the binding reference
        _binding = null
    }
}
