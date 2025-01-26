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
import com.example.neighborhub.databinding.FragmentRegisterBinding
import com.example.neighborhub.ui.viewmodel.RegisterViewModel
import com.example.neighborhub.repository.AuthRepository
import com.example.neighborhub.ui.viewmodel.RegisterViewModelFactory

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(AuthRepository()) // Pass AuthRepository to the factory
    }

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            // Validate input fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Register user via ViewModel
            registerViewModel.registerUser(email, password).observe(viewLifecycleOwner, Observer { result ->
                if (result != null) {
                    // Check if the registration was successful
                    if (result.contains("successful")) {
                        Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
                    } else {
                        // Registration failed, show the specific error message
                        Toast.makeText(requireContext(), "Registration failed: $result", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
