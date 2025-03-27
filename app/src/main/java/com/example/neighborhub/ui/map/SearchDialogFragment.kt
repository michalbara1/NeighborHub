package com.example.neighborhub.ui.map

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.neighborhub.R

class SearchDialogFragment(
    private val onSearchApplied: (Map<String, Any>) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.search_dialog_fragment, null)

        // Get references to views with the correct IDs that match your XML
        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)
        val within500MetersCheckbox = view.findViewById<CheckBox>(R.id.within500MetersCheckbox)
        val applyButton = view.findViewById<Button>(R.id.applyButton)
        val showAllButton = view.findViewById<Button>(R.id.showAllButton)
        val goBackButton = view.findViewById<Button>(R.id.goBackButton)

        // Apply Search button logic
        applyButton.setOnClickListener {
            val filters = mapOf(
                "searchQuery" to searchEditText.text.toString(),
                "within500Meters" to within500MetersCheckbox.isChecked,
                "action" to "search"
            )
            onSearchApplied(filters)
            dismiss()
        }

        // Show All button logic
        showAllButton.setOnClickListener {
            val filters = mapOf(
                "within500Meters" to within500MetersCheckbox.isChecked,
                "action" to "showAll"
            )
            onSearchApplied(filters)
            dismiss()
        }

        // Go Back button logic
        goBackButton.setOnClickListener {
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }
}