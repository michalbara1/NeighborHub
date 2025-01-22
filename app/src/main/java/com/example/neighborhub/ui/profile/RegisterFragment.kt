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

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val registerViewModel: RegisterViewModel by viewModels()

    // ViewBinding for the Register Fragment UI
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
            registerViewModel.registerUser(email, password).observe(viewLifecycleOwner, Observer { isSuccess ->
                if (isSuccess) {
                    // Registration successful, show a toast message
                    Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                } else {
                    // Registration failed, show a toast message
                    Toast.makeText(requireContext(), "Registration failed. Try again", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks by cleaning up the binding reference
        _binding = null
    }
}
