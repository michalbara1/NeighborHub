package com.example.neighborhub.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.test.services.events.ErrorInfo
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.example.neighborhub.databinding.FragmentEditProfileBinding
import com.example.neighborhub.ui.viewmodel.ProfileViewModel
import com.example.neighborhub.ui.viewmodel.ProfileViewModelFactory
import com.example.neighborhub.repository.AuthRepository

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(AuthRepository())
    }

    private var profilePictureUrl: String? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImageToCloudinary(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uploadPictureButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.saveProfileBtn.setOnClickListener {
            val newUserName = binding.editUserName.text.toString()
            val newUserEmail = binding.editUserEmail.text.toString()
            viewModel.updateUserDetails(newUserName, newUserEmail, profilePictureUrl)
        }
    }

    private fun uploadImageToCloudinary(uri: Uri) {
        MediaManager.get().upload(uri).callback(object : UploadCallback {
            override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                profilePictureUrl = resultData?.get("secure_url") as String
                Glide.with(this@EditProfileFragment)
                    .load(profilePictureUrl)
                    .into(binding.profilePicturePreview)
                Toast.makeText(requireContext(), "Profile picture uploaded!", Toast.LENGTH_SHORT).show()
            }

            override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                Toast.makeText(requireContext(), "Upload failed: ${error?.description}", Toast.LENGTH_SHORT).show()
            }

            override fun onStart(requestId: String?) {}
            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
            override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {}
        }).dispatch()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}