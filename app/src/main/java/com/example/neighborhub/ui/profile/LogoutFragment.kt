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
import com.example.neighborhub.databinding.FragmentLogoutBinding
import com.example.neighborhub.ui.viewmodel.LogoutViewModel
import com.example.neighborhub.ui.viewmodel.LogoutViewModelFactory
import com.example.neighborhub.repository.AuthRepository

class LogoutFragment : Fragment(R.layout.fragment_logout) {

    // Use the custom factory to provide AuthRepository to the LogoutViewModel
    private val logoutViewModel: LogoutViewModel by viewModels {
        LogoutViewModelFactory(AuthRepository()) // Provide the factory here
    }

    // ViewBinding for the Logout Fragment UI
    private var _binding: FragmentLogoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutButton.setOnClickListener {
            // Call the logout function via the ViewModel
            logoutViewModel.logoutUser()

            // Show a toast confirming the logout
            Toast.makeText(requireContext(), "You have logged out", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks by cleaning up the binding reference
        _binding = null
    }
}
