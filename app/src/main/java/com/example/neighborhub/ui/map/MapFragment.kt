package com.example.neighborhub.ui.map

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.neighborhub.ui.viewmodel.PostViewModel
import com.example.neighborhub.ui.viewmodel.PostViewModelFactory
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
    private val PostMarkers = mutableMapOf<String, Marker>()

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




        // Observe reviews from ViewModel
        postViewModel._posts.observe(viewLifecycleOwner) { posts ->
            displayPostsOnMap(posts)
        }

        // Replace the current reviews observer with this:
        postViewModel._posts.observe(viewLifecycleOwner) { posts ->
            Log.d("MapFragment", "Posts updated: ${posts.size}")
            updateMapMarkers(posts)
            showUserLocation() // Ensure user location marker stays on top
        }




//        // Observe posts from ViewModel
//        postViewModel.filteredPosts.observe(viewLifecycleOwner) { posts ->
//            updateMapMarkers(posts)
//
//            // If we have a post ID to focus on, find and highlight it
//            if (args.postId.isNotEmpty()) {
//                focusOnPost(args.postId, posts)
//            }
//        }
//
//        // Fetch posts
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
        // Set a default center location if no location is available
        mapView.controller.setCenter(GeoPoint(37.7749, -122.4194)) // San Francisco placeholder
    }

    // Getting the location premissions
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                showUserLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Snackbar.make(binding.root, "Location permission is required to show your location on the map.", Snackbar.LENGTH_INDEFINITE).setAction("Grant") {
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

    private fun getCurrentUserLocation(onLocationReady: (GeoPoint?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
            requireContext(), // Use requireContext() instead of 'this'
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(), // Use requireContext() instead of 'this'
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationReady(GeoPoint(location.latitude, location.longitude))
            } else {
                Log.e("MapFragment", "Location is null.")
                Snackbar.make(binding.root, "Unable to determine your location.", Snackbar.LENGTH_SHORT).show()
                onLocationReady(null) // Pass null if location is unavailable
            }
        }.addOnFailureListener { exception ->
            Log.e("MapFragment", "Failed to retrieve location: ${exception.message}", exception)
            Snackbar.make(binding.root, "Failed to retrieve your location.", Snackbar.LENGTH_SHORT).show()
            onLocationReady(null) // Pass null on failure
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d("MapFragment", "User location: ${location.latitude}, ${location.longitude}")
                val userLocation = GeoPoint(location.latitude, location.longitude)
                // Add marker for user location
                val marker = Marker(mapView).apply {
                    position = userLocation
                    title = "You are here" // Present it when tapping the marker
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                // Remove existing "self-location" markers to avoid duplication
                mapView.overlays.removeIf { it is Marker && it.title == "You are here" }
                mapView.overlays.add(marker)
                Log.d("MapFragment", "Marker added: ${marker.title}")
                // Center map on user location
                mapView.controller.setCenter(userLocation)
                mapView.controller.setZoom(20.0)
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

        mapView.invalidate()
    }

    private fun displayPostsOnMap(posts: List<Post>) {
        Log.d("MapFragment", "Displaying posts on map. Existing overlays: ${mapView.overlays.size}")

        // Step 1: Remove markers for posts no longer present
        val existingPostIds = posts.map { it.id }
        val markersToRemove = PostMarkers.keys.filterNot { it in existingPostIds }
        for (postId in markersToRemove) {
            PostMarkers[postId]?.let { marker ->
                mapView.overlays.remove(marker)
            }
            PostMarkers.remove(postId)
        }

        // Step 2: Add or update markers for current posts
        for (post in posts) {
            // Direct access to the Post model properties
            val postId = post.id
            val latitude = post.latitude
            val longitude = post.longitude
            val title = post.headline // Assuming `headline` is used as the title
            val content = post.content // Optional: Add as snippet or description

            // Ensure the latitude and longitude are not null before proceeding
            if (latitude == null || longitude == null) continue

            // Check if the marker already exists for this postId
            val existingMarker = PostMarkers[postId]

            if (existingMarker == null) {
                // Add new marker
                val marker = Marker(mapView).apply {
                    position = GeoPoint(latitude, longitude)
                    this.title = title
                    snippet = content // Optional: Add description to the marker
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    // Set click listener for the marker
                    setOnMarkerClickListener { clickedMarker, _ ->
                        if (clickedMarker.infoWindow != null && clickedMarker.isInfoWindowOpen) {
                            // If the InfoWindow is already open, proceed to show the dialog
                            showPostDialog(postId)
                        } else {
                            // Show the title by displaying the InfoWindow
                            clickedMarker.showInfoWindow()
                        }
                        true // Consume the click event
                    }
                }

                mapView.overlays.add(marker)
                PostMarkers[postId] = marker
                Log.d("MapFragment", "Post marker added: $title at $latitude, $longitude")
            } else {
                // Update existing marker (if needed)
                existingMarker.position = GeoPoint(latitude, longitude)
                existingMarker.title = title
                existingMarker.snippet = content
                // Refresh click listener for the updated marker
                existingMarker.setOnMarkerClickListener { _, _ ->
                    showPostDialog(postId)
                    true // Consume the click event
                }
                Log.d("MapFragment", "Marker click listener updated for post: $title")
            }
        }

        Log.d("MapFragment", "Final overlays after adding posts: ${mapView.overlays.size}")
        mapView.invalidate()
    }


    private fun showPostDialog(postId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Post Options")
        builder.setMessage("What would you like to do?")

        builder.setPositiveButton("View Post") { _, _ ->
            navigateToPostDetails(postId)
        }
        builder.setNegativeButton("Go Back") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun navigateToPostDetails(postId: String) {
        val action = MapFragmentDirections.actionMapFragmentToPostDetailsFragment(postId)
        findNavController().navigate(action)
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
