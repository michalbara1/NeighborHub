package com.example.neighborhub.ui.post

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.neighborhub.R

class CreatePostFragment : Fragment(R.layout.fragment_create_post) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleEditText = view.findViewById<EditText>(R.id.etTitle)
        val descriptionEditText = view.findViewById<EditText>(R.id.etDescription)
        val postTypeSpinner = view.findViewById<Spinner>(R.id.spinnerPostType)
        val submitButton = view.findViewById<Button>(R.id.btnSubmit)

        val postTypes = listOf("message", "question", "proposal")
        postTypeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, postTypes)

        submitButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val postType = postTypeSpinner.selectedItem.toString()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                Toast.makeText(requireContext(), "Published: $postType", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
