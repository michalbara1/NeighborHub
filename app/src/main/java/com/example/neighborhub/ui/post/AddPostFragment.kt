package com.example.neighborhub.ui.post

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.neighborhub.R
import com.example.neighborhub.databinding.FragmentAddPostBinding
import com.example.neighborhub.model.Post
import com.example.neighborhub.ui.viewmodel.AddPostViewModel
import com.example.neighborhub.ui.viewmodel.AddPostViewModelFactory
import com.example.neighborhub.ui.viewmodel.EmojiViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class AddPostFragment : Fragment() {
    // Declare latitude and longitude only once at the class level
    private var latitude: Double? = null
    private var longitude: Double? = null

    private lateinit var binding: FragmentAddPostBinding
    private val viewModel: AddPostViewModel by viewModels {
        AddPostViewModelFactory(requireContext())
    }
    private val emojiViewModel: EmojiViewModel by activityViewModels()

    private var imageUri: Uri? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                binding.postImageView.visibility = View.VISIBLE
                Glide.with(requireContext()).load(it).into(binding.postImageView)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Check and request location permission
        checkLocationPermission()

        // Set up click listeners
        setupClickListeners()

        // Fetch and display current user info
        fetchCurrentUserInfo()

        // Observe ViewModel
        observeViewModel()
    }

    private fun checkLocationPermission() {
        when {
            // Check if location permission is granted
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getLocation()  // Fetch location if permission is granted
            }
            else -> {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getLocation()  // Fetch location if permission is granted
        } else {
            Snackbar.make(binding.root, "Location permission denied.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                binding.locationInfoLayout.visibility = View.VISIBLE
                binding.locationText.text = "Location: ${String.format("%.6f, %.6f", latitude, longitude)}"
            } else {
                Snackbar.make(binding.root, "Unable to fetch location", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        // Profile image click
        binding.imageProfile.setOnClickListener {
            findNavController().navigate(R.id.action_addPostFragment_to_profileFragment)
        }

        // Image upload button
        binding.uploadImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Add emoji button
        binding.addEmojiButton.setOnClickListener {
            findNavController().navigate(R.id.action_addPostFragment_to_emojiPickerFragment)
        }

        // Location clear button
        binding.clearLocationButton.setOnClickListener {
            latitude = null
            longitude = null
            binding.locationInfoLayout.visibility = View.GONE
        }

        // Submit button
        binding.submitButton.setOnClickListener {
            submitPost()
        }
    }

    private fun fetchCurrentUserInfo() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get user data
                        val username = document.getString("username") ?: "Unknown User"
                        val profileImageUrl = document.getString("profilePictureUrl") ?: ""

                        // Update UI with user data
                        binding.userNameTextView.text = username

                        // Load profile image
                        if (profileImageUrl.isNotEmpty()) {
                            Glide.with(requireContext())
                                .load(profileImageUrl)
                                .circleCrop()
                                .into(binding.imageProfile)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AddPostFragment", "Failed to get user data", e)
                }
        }
    }

    private fun observeViewModel() {
        // Post ViewModel observers
        viewModel.success.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Post added successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        // Emoji observer
        emojiViewModel.selectedEmoji.observe(viewLifecycleOwner) { emoji ->
            emoji?.let {
                if (it.unicode.isNotEmpty()) {
                    val unicodeString = convertUnicodeToEmoji(it.unicode.firstOrNull() ?: "")
                    if (unicodeString.isNotEmpty()) {
                        binding.selectedEmojiTextView.text = unicodeString
                        binding.selectedEmojiTextView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun convertUnicodeToEmoji(unicodeStr: String): String {
        if (unicodeStr.isEmpty()) return ""
        return try {
            unicodeStr.replace("U+", "")  // Remove U+ prefix but don't add 0x
                .split(" ")
                .filter { it.isNotEmpty() }
                .map {
                    try {
                        Integer.parseInt(it, 16)  // Parse as hex
                    } catch (e: NumberFormatException) {
                        Log.e("AddPostFragment", "Failed to parse: $it", e)
                        0x1F60A  // Fallback to smiling face emoji
                    }
                }
                .map {
                    try {
                        Character.toChars(it)
                    } catch (e: Exception) {
                        Log.e("AddPostFragment", "Failed to convert: $it to char", e)
                        Character.toChars(0x1F60A)  // Fallback to smiling face emoji
                    }
                }
                .joinToString("") { it.concatToString() }
        } catch (e: Exception) {
            Log.e("AddPostFragment", "Error converting unicode to emoji", e)
            "😊" // Fallback emoji
        }
    }

    private fun submitPost() {
        Log.d("AddPostFragment", "Submit button clicked")
        val headline = binding.headlineInput.text.toString()
        val content = binding.contentInput.text.toString()

        if (headline.isBlank() || content.isBlank()) {
            Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") ?: "Unknown User"
                        val profileImageUrl = document.getString("profilePictureUrl") ?: ""

                        Log.d("AddPostFragment", "Creating post with username: $username and profile image: $profileImageUrl")

                        val post = Post(
                            id = UUID.randomUUID().toString(),
                            headline = headline,
                            content = content,
                            userName = username,
                            userPhotoUrl = profileImageUrl,
                            userId = currentUserId,
                            emojiUnicode = emojiViewModel.selectedEmoji.value?.unicode?.firstOrNull(),
                            emojiName = emojiViewModel.selectedEmoji.value?.name,
                            latitude = latitude,
                            longitude = longitude,
                            lastUpdated = System.currentTimeMillis()
                        )

                        if (imageUri != null) {
                            Log.d("AddPostFragment", "Uploading image")
                            viewModel.uploadImage(imageUri!!) { imageUrl ->
                                if (imageUrl != null) {
                                    Log.d("AddPostFragment", "Image uploaded successfully: $imageUrl")
                                    post.imageUrl = imageUrl
                                    viewModel.addPost(post)
                                } else {
                                    Log.e("AddPostFragment", "Failed to upload image")
                                    Toast.makeText(requireContext(), "Failed to upload image.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Log.d("AddPostFragment", "Adding post without image")
                            viewModel.addPost(post)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AddPostFragment", "Failed to get user data", e)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "You need to be logged in to post", Toast.LENGTH_SHORT).show()
        }
    }
}
