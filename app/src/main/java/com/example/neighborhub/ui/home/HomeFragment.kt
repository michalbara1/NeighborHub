package com.example.neighborhub.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.neighborhub.R
import com.example.neighborhub.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    // ViewBinding for the Home Fragment UI
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add a delay to automatically navigate to the LoginFragment after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToLogin()
        }, 3000) // 3000 milliseconds = 3 seconds

        // Optional: Add a click listener to the ImageView for manual navigation
        binding.imageView.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        // Navigate to the LoginFragment
        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the binding reference to avoid memory leaks
        _binding = null
    }
}