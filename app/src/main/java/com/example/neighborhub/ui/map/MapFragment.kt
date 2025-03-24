package com.example.neighborhub.ui.map

import android.Manifest
import android.app.AlertDialog
import androidx.navigation.fragment.navArgs
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.neighborhub.R
import com.example.neighborhub.databinding.MapFragmentBinding
import com.example.neighborhub.model.Post
import com.example.neighborhub.ui.map.FilterDialogFragment
import com.example.neighborhub.ui.viewmodel.PostViewModel
import com.example.neighborhub.utils.NetworkUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapFragment : Fragment() {

    private val args: MapFragmentArgs by navArgs()
    private lateinit var mapView: MapView
    private var _binding: MapFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var longPressHandler: Handler? = null
    private var longPressRunnable: Runnable? = null
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
            requireContext(),
            requireContext().getSharedPreferences("osm_prefs", 0)
        )
        _binding = MapFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java]

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
            if (args.focusPostId.isNotEmpty()) {
                focusOnPost(args.focusPostId, posts)
            }
        }

        // Fetch posts
        postViewModel.fetchPosts()

        // Add Bottom Navigation Actions
        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            handleBottomNavigation(item)
        }

        // Handle long press on the map
        setupLongPressListener()

        // Check if we need to focus on a specific location
        if (args.focusLatitude != 0f && args.focusLongitude != 0f) {
            val focusPoint = GeoPoint(args.focusLatitude.toDouble(), args.focusLongitude.toDouble())
            mapView.controller.setCenter(focusPoint)
            mapView.controller.setZoom(18.0)
        }
    }

    private fun focusOnPost(postId: String, posts: List<Post>) {
        // Find the post in the list
        val post = posts.find { it.id == postId } ?: return

        // Make sure it has location data
        if (post.latitude == null || post.longitude == null) return

        // Center the map on the post
        val postLocation = GeoPoint(post.latitude!!, post.longitude!!)
        mapView.controller.animateTo(postLocation)
        mapView.controller.setZoom(18.0)

        // Find and highlight the marker
        postMarkers[postId]?.let { marker ->
            marker.showInfoWindow()
            // Optionally change the marker style to highlight it
        }
    }

    // Setting up the map
    private fun setupMapView() {
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        // Set a default center location if no location is available
        mapView.controller.setCenter(GeoPoint(37.7749, -122.4194)) // San Francisco placeholder
    }

    // Getting the location permissions
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                showUserLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(
                    binding.root,
                    "Location permission is required to show your location on the map.",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Grant") {
                    requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            }
            else -> {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Request the location permissions
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showUserLocation()
        } else {
            Snackbar.make(binding.root, "Location permission denied.", Snackbar.LENGTH_SHORT).show()
        }
    }

    // Showing the user's location on the map
    private fun showUserLocation() {
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

                // Remove existing user location markers to avoid duplication
                mapView.overlays.removeIf { it is Marker && it.relatedObject == "UserLocation" }
                mapView.overlays.add(marker)

                // Center map on user location
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

    // Bottom toolbar interface code
    private fun handleBottomNavigation(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filterButton -> {
                val dialog = FilterDialogFragment { action, filters ->
                    when (action) {
                        "applyFilters" -> {
                            filters?.let {
                                applyFiltersWithDistance(it)
                            }
                        }
                        "showAll" -> {
                            filters?.let {
                                val within500Meters = it["within500Meters"] as? Boolean ?: false
                                if (within500Meters) {
                                    showPostsWithinDistance(500.0)
                                } else {
                                    postViewModel.fetchPosts()
                                }
                            }
                        }
                        "hideAll" -> {
                            clearAllMarkers()
                        }
                    }
                }
                dialog.show(parentFragmentManager, "FilterDialog")
                true
            }
            R.id.goBackButton -> {
                findNavController().navigateUp()
                true
            }
            else -> false
        }
    }

    private fun applyFiltersWithDistance(filters: Map<String, Any>) {
        val within500Meters = filters["within500Meters"] as? Boolean ?: false

        if (within500Meters && userLocation != null) {
            postViewModel.filterPostsByDistance(
                userLocation!!.latitude,
                userLocation!!.longitude,
                500.0
            )
        } else {
            // No distance filtering, just fetch all posts
            postViewModel.fetchPosts()
        }
    }

    private fun showPostsWithinDistance(distance: Double) {
        if (userLocation != null) {
            postViewModel.filterPostsByDistance(
                userLocation!!.latitude,
                userLocation!!.longitude,
                distance
            )
        } else {
            Snackbar.make(
                binding.root,
                "Unable to determine your location for filtering.",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun clearAllMarkers() {
        // Remove all post markers but keep user location marker
        mapView.overlays.removeIf { overlay ->
            overlay is Marker && overlay.relatedObject != "UserLocation"
        }
        postMarkers.clear()
        mapView.invalidate()
    }

    private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            point1.latitude, point1.longitude,
            point2.latitude, point2.longitude,
            results
        )
        return results[0].toDouble() // Return distance in meters
    }

    // Update map markers based on posts
    private fun updateMapMarkers(posts: List<Post>) {
        // Clear existing post markers (but keep user location)
        mapView.overlays.removeIf { overlay ->
            overlay is Marker && overlay.relatedObject != "UserLocation"
        }
        postMarkers.clear()

        // Add markers for posts
        for (post in posts) {
            // Skip posts without location data
            if (post.latitude == null || post.longitude == null) continue

            val markerIcon = when {
                post.emojiName?.contains("happy", ignoreCase = true) == true ||
                        post.emojiName?.contains("smile", ignoreCase = true) == true ||
                        post.emojiName?.contains("good", ignoreCase = true) == true ->
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_green_marker)

                post.emojiName?.contains("angry", ignoreCase = true) == true ||
                        post.emojiName?.contains("sad", ignoreCase = true) == true ||
                        post.emojiName?.contains("bad", ignoreCase = true) == true ->
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_red_marker)

                else -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_default_marker)
            }

            val marker = Marker(mapView).apply {
                position = GeoPoint(post.latitude!!, post.longitude!!)
                title = post.headline
                icon = markerIcon
                relatedObject = post.id // Associate postId with the marker

                setOnMarkerClickListener { clickedMarker, _ ->
                    val postId = clickedMarker.relatedObject as? String ?: return@setOnMarkerClickListener true

                    if (clickedMarker.isInfoWindowOpen) {
                        // If info window is open, show post details
                        showPostDialog(postId)
                    } else {
                        // Otherwise, just show the info window
                        clickedMarker.showInfoWindow()
                    }
                    true // Consume the click event
                }
            }

            mapView.overlays.add(marker)
            postMarkers[post.id] = marker
            Log.d("MapFragment", "Post marker added: ${post.headline} at ${post.latitude}, ${post.longitude}")
        }

        mapView.invalidate() // Refresh the map
    }

    // Activating the long press on the map to add a post
    private fun setupLongPressListener() {
        mapView.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    startLongPressHandler(motionEvent)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    cancelLongPressHandler()
                }
            }
            false
        }
    }

    // Long press interface
    private fun startLongPressHandler(motionEvent: MotionEvent) {
        longPressHandler = Handler(Looper.getMainLooper())
        longPressRunnable = Runnable {
            val geoPoint = mapView.projection.fromPixels(
                motionEvent.x.toInt(),
                motionEvent.y.toInt()
            ) as GeoPoint
            showAddPostDialog(geoPoint)
        }
        longPressHandler?.postDelayed(longPressRunnable!!, 1000) // 1 second delay
    }

    // Cancel long press handler
    private fun cancelLongPressHandler() {
        longPressHandler?.removeCallbacks(longPressRunnable!!)
        longPressHandler = null
        longPressRunnable = null
    }

    // Show dialog to add a new post at the selected location
    private fun showAddPostDialog(location: GeoPoint) {
        if (NetworkUtils.isOnline(requireContext())) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Add Post")
            builder.setMessage("Would you like to create a post for this location?")
            builder.setPositiveButton("Yes") { _, _ ->
                // Navigate to create post fragment with location data
                val action = MapFragmentDirections.actionMapFragmentToAddPostFragment(
                    location.latitude.toFloat(),
                    location.longitude.toFloat()
                )
                findNavController().navigate(action)
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        } else {
            Toast.makeText(requireContext(), "You cannot add posts when offline.", Toast.LENGTH_SHORT).show()
        }
    }

    // Show dialog when a post marker is clicked
    private fun showPostDialog(postId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Post Options")
        builder.setMessage("What would you like to do?")

        builder.setPositiveButton("View Post") { _, _ ->
            val action = MapFragmentDirections.actionMapFragmentToPostDetailsFragment(postId)
            findNavController().navigate(action)
        }

        builder.setNegativeButton("Go Back") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

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
}