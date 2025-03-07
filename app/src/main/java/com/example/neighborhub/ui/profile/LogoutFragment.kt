package com.example.neighborhub.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.neighborhub.R
import com.example.neighborhub.databinding.FragmentLogoutBinding
import com.example.neighborhub.ui.viewmodel.UserViewModel

class LogoutFragment : Fragment(R.layout.fragment_logout) {

    private val userViewModel: UserViewModel by viewModels()

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
            val userId = userViewModel.getCurrentUser()?.uid
            if (userId != null) {
                userViewModel.logout(userId)
                Toast.makeText(requireContext(), "You have logged out", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_logoutFragment_to_loginFragment)
            } else {
                Toast.makeText(requireContext(), "No user is currently logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}