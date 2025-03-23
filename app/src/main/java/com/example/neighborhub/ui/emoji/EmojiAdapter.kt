package com.example.neighborhub.ui.emoji

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.neighborhub.api.Emoji
import com.example.neighborhub.databinding.ItemEmojiBinding

class EmojiAdapter(private val onEmojiSelected: (Emoji) -> Unit) :
    ListAdapter<Emoji, EmojiAdapter.EmojiViewHolder>(EmojiDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val binding = ItemEmojiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmojiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        holder.bind(getItem(position), onEmojiSelected)
    }

    class EmojiViewHolder(private val binding: ItemEmojiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(emoji: Emoji, onEmojiSelected: (Emoji) -> Unit) {
            // Display emoji name
            binding.textEmojiName.text = emoji.name

            // Display emoji category
            binding.textEmojiCategory.text = emoji.category

            // Try to display the actual emoji using Unicode
            if (emoji.unicode.isNotEmpty()) {
                try {
                    // Fix the unicode parsing
                    val unicodeString = emoji.unicode[0]
                        .replace("U+", "")
                        .split(" ")
                        .filter { it.isNotEmpty() }
                        .map {
                            try {
                                Integer.parseInt(it, 16)
                            } catch (e: NumberFormatException) {
                                Log.e("EmojiAdapter", "Failed to parse: $it", e)
                                0 // Fallback
                            }
                        }
                        .map {
                            try {
                                Character.toChars(it)
                            } catch (e: Exception) {
                                Log.e("EmojiAdapter", "Failed to convert: $it to char", e)
                                charArrayOf(' ') // Fallback to space
                            }
                        }
                        .joinToString("") { it.concatToString() }

                    binding.textEmojiSymbol.text = unicodeString
                } catch (e: Exception) {
                    Log.e("EmojiAdapter", "Error converting unicode to emoji", e)
                    binding.textEmojiSymbol.text = "😊" // Fallback emoji
                }
            } else {
                binding.textEmojiSymbol.text = ""
            }

            // Set click listener
            binding.root.setOnClickListener {
                onEmojiSelected(emoji)
            }
        }
    }

    private class EmojiDiffCallback : DiffUtil.ItemCallback<Emoji>() {
        override fun areItemsTheSame(oldItem: Emoji, newItem: Emoji): Boolean {
            // Since Emoji API doesn't provide ID, compare by name and unicode
            return oldItem.name == newItem.name &&
                    oldItem.unicode.firstOrNull() == newItem.unicode.firstOrNull()
        }

        override fun areContentsTheSame(oldItem: Emoji, newItem: Emoji): Boolean {
            return oldItem == newItem
        }
    }
}