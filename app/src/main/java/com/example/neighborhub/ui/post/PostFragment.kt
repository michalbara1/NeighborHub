package com.example.neighborhub.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.neighborhub.R

class PostFragment : Fragment() {

    private lateinit var etTitle: EditText
    private lateinit var etType: EditText
    private lateinit var etDescription: EditText
    private lateinit var etLocation: EditText
    private lateinit var btnSubmit: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_post, container, false)

        // Initialize views
        etTitle = view.findViewById(R.id.etTitle)
        etType = view.findViewById(R.id.etType)
        etDescription = view.findViewById(R.id.etDescription)
        etLocation = view.findViewById(R.id.etLocation)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            handleSubmit()
        }

        return view
    }

    private fun handleSubmit() {
        val title = etTitle.text.toString().trim()
        val type = etType.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val location = etLocation.text.toString().trim()

        if (title.isEmpty() || type.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
        } else {
            // Handle form submission logic (e.g., API call, database storage, etc.)
            Toast.makeText(requireContext(), "Request submitted successfully!", Toast.LENGTH_SHORT).show()
            clearFields()
        }
    }

    private fun clearFields() {
        etTitle.text.clear()
        etType.text.clear()
        etDescription.text.clear()
        etLocation.text.clear()
    }
}
