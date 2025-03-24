package com.example.neighborhub.ui.map

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import com.example.neighborhub.R

class FilterDialogFragment(
    private val onFilterAction: (String, Map<String, Any>?) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.filter_dialog, null)

        val within500MetersCheckbox = view.findViewById<CheckBox>(R.id.within500MetersCheckbox)
        val applyFiltersButton = view.findViewById<Button>(R.id.applyButton)
        val showAllButton = view.findViewById<Button>(R.id.showAllButton)
        val hideAllButton = view.findViewById<Button>(R.id.hideAllButton)
        val goBackButton = view.findViewById<Button>(R.id.goBackButton)

        // Apply Filters button logic
        applyFiltersButton.setOnClickListener {
            val filters = mutableMapOf<String, Any>(
                "within500Meters" to within500MetersCheckbox.isChecked
            )
            onFilterAction("applyFilters", filters)
            dismiss()
        }

        // Show All button logic
        showAllButton.setOnClickListener {
            val filters = mapOf("within500Meters" to within500MetersCheckbox.isChecked)
            onFilterAction("showAll", filters)
            dismiss()
        }

        // Hide All button logic
        hideAllButton.setOnClickListener {
            onFilterAction("hideAll", null)
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