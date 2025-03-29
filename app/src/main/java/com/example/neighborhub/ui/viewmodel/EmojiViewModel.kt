package com.example.neighborhub.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neighborhub.api.Emoji
import com.example.neighborhub.repository.EmojiRepository
import kotlinx.coroutines.launch

class EmojiViewModel : ViewModel() {
    private val repository = EmojiRepository()

    private val _emojis = MutableLiveData<List<Emoji>>()
    val emojis: LiveData<List<Emoji>> = _emojis

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _selectedEmoji = MutableLiveData<Emoji?>(null)
    val selectedEmoji: LiveData<Emoji?> = _selectedEmoji

    fun loadAllEmojis() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getAllEmojis()
                .onSuccess { emojiList ->
                    _emojis.value = emojiList
                    Log.d("EmojiViewModel", "Loaded ${emojiList.size} emojis")
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Unknown error occurred"
                    Log.e("EmojiViewModel", "Error loading emojis", exception)
                }

            _isLoading.value = false
        }
    }

    fun searchEmojis(query: String) {
        if (query.isEmpty()) {
            loadAllEmojis()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.searchEmojis(query)
                .onSuccess { emojiList ->
                    _emojis.value = emojiList
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Unknown error occurred"
                }

            _isLoading.value = false
        }
    }

    fun selectEmoji(emoji: Emoji) {
        Log.d("EmojiViewModel", "Emoji selected: ${emoji.name}, unicode: ${emoji.unicode.firstOrNull()}")
        _selectedEmoji.value = emoji
    }

    fun clearSelectedEmoji() {
        _selectedEmoji.value = null
    }


    fun getSelectedEmojiUnicode(): String? {
        return _selectedEmoji.value?.unicode?.firstOrNull()
    }
}