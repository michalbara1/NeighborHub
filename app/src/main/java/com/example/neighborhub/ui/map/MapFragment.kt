package com.example.neighborhub.ui.map

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.neighborhub.R
import com.example.neighborhub.databinding.MapFragmentBinding
import com.example.neighborhub.model.Post
import com.example.neighborhub.repository.PostRepository
import com.example.neighborhub.ui.map.FilterDialogFragment
import com.example.neighborhub.ui.viewmodel.PostViewModel
import com.example.neighborhub.ui.viewmodel.PostViewModelFactory
import com.example.neighborhub.utils.NetworkUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapFragment : Fragment() {

    private val args: MapFragmentArgs by navArgs()  // Access arguments (including postId)
    private lateinit var mapView: MapView
    private var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var postViewModel: PostViewModel
    private val postMarkers = mutableMapOf<String, Marker>()
    private var userLocation: GeoPoint? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize map configuration
        Configuration.getInstance().load(
            requireActivity(), // Use requireActivity() instead of requireContext()
            requireActivity().getSharedPreferences("osm_prefs", 0)
        )
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!::postViewModel.isInitialized) {
            val postRepository = PostRepository(requireContext())
            postViewModel = ViewModelProvider(this, PostViewModelFactory(postRepository)).get(PostViewModel::class.java)
        }


        // Initialize the Map
        mapView = binding.mapView
        setupMapView()

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        checkLocationPermission()

        // Observe posts from ViewModel
        postViewModel.filteredPosts.observe(viewLifecycleOwner) { posts ->
            updateMapMarkers(posts)

            // If we have a post ID to focus on, find and highlight it
            if (args.postId.isNotEmpty()) {
                focusOnPost(args.postId, posts)
            }
        }

        // Fetch posts
        postViewModel.fetchPosts()
    }

    private fun focusOnPost(postId: String, posts: List<Post>) {
        val post = posts.find { it.id == postId } ?: return
        if (post.latitude == null || post.longitude == null) return

        val postLocation = GeoPoint(post.latitude!!, post.longitude!!)
        mapView.controller.animateTo(postLocation)
        mapView.controller.setZoom(18.0)

        postMarkers[postId]?.let { marker ->
            marker.showInfoWindow()
        }
    }

    private fun setupMapView() {
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(37.7749, -122.4194)) // Default location
    }

    // Permissions and Location handling (same as before)

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        postViewModel.fetchPosts() // Refresh posts
        showUserLocation() // Update user location
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Check for location permission
    private fun checkLocationPermission() {
        when {
            // Check if location permission is granted
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                showUserLocation()  // Proceed if permission granted
            }
            // If we need to show rationale to the user
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(
                    binding.root,
                    "Location permission is required to show your location on the map.",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Grant") {
                    requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            }
            // If permission hasn't been granted, request it
            else -> {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Request location permission
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showUserLocation()  // Show location if permission is granted
        } else {
            Snackbar.make(binding.root, "Location permission denied.", Snackbar.LENGTH_SHORT).show()
        }
    }

    // Show the user's location on the map
    private fun showUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), // Use requireContext() instead of 'this'
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), // Use requireContext() instead of 'this'
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the missing permissions here
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d("MapFragment", "User location: ${location.latitude}, ${location.longitude}")
                userLocation = GeoPoint(location.latitude, location.longitude)

                // Add marker for user location
                val marker = Marker(mapView).apply {
                    position = userLocation
                    title = "You are here"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    relatedObject = "UserLocation" // Tag to identify user location marker
                }

                // Remove any existing user location markers to avoid duplication
                mapView.overlays.removeIf { it is Marker && it.relatedObject == "UserLocation" }
                mapView.overlays.add(marker)

                // Center map on user's location
                mapView.controller.setCenter(userLocation)
                mapView.controller.setZoom(18.0)
                mapView.invalidate()
            } else {
                Log.e("MapFragment", "Location is null.")
                Snackbar.make(binding.root, "Unable to determine your location.", Snackbar.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("MapFragment", "Failed to retrieve location: ${exception.message}", exception)
            Snackbar.make(binding.root, "Failed to retrieve your location.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun updateMapMarkers(posts: List<Post>) {
        // Remove all existing markers
        mapView.overlays.clear()

        // Add markers for each post
        posts.forEach { post ->
            // Check if the post has valid coordinates
            if (post.latitude != null && post.longitude != null) {
                val postLocation = GeoPoint(post.latitude!!, post.longitude!!)

                val marker = Marker(mapView).apply {
                    position = postLocation
                    title = post.headline  // You can change the title as needed
                    snippet = post.content // Optional: Add a description as a snippet
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }

                // Store the marker in the map
                postMarkers[post.id] = marker

                // Add the marker to the map
                mapView.overlays.add(marker)
            }
        }

        // Optionally, refresh the map view after adding markers
        mapView.invalidate()
    }
}
