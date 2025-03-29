package com.example.neighborhub.ui.emoji

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.neighborhub.databinding.FragmentEmojiPickerBinding
import com.example.neighborhub.ui.viewmodel.EmojiViewModel

class EmojiPickerFragment : Fragment() {
    private var _binding: FragmentEmojiPickerBinding? = null
    private val binding get() = _binding!!


    private val viewModel: EmojiViewModel by activityViewModels()
    private lateinit var emojiAdapter: EmojiAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmojiPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupObservers()


        viewModel.loadAllEmojis()
    }

    private fun setupRecyclerView() {
        emojiAdapter = EmojiAdapter { emoji ->

            Log.d("EmojiPickerFragment", "Emoji selected: ${emoji.name}, unicode: ${emoji.unicode.firstOrNull()}")
            viewModel.selectEmoji(emoji)


            Log.d("EmojiPickerFragment", "Selected emoji in ViewModel: ${viewModel.selectedEmoji.value?.name}")


            findNavController().popBackStack()
        }

        binding.recyclerViewEmojis.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = emojiAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchViewEmojis.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchEmojis(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    viewModel.loadAllEmojis()
                }
                return true
            }
        })
    }

    private fun setupObservers() {
        viewModel.emojis.observe(viewLifecycleOwner) { emojis ->
            Log.d("EmojiPickerFragment", "Loaded ${emojis.size} emojis")
            emojiAdapter.submitList(emojis)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                // Show error message
                binding.textViewError.apply {
                    text = errorMessage
                    visibility = View.VISIBLE
                }
            } else {
                binding.textViewError.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}