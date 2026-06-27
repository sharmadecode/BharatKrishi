package com.bharatkrishi.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bharatkrishi.app.ai.GeminiApiClient
import com.bharatkrishi.app.ai.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AIChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val repository = GeminiRepository(GeminiApiClient.api)

    fun sendMessage(text: String, image: android.graphics.Bitmap? = null) {
        val displayMessage = if (text.isBlank() && image != null) "Sent an image for analysis" else text
        
        val userMessage = ChatMessage(displayMessage, true, "Now", image)
        _messages.value = _messages.value + userMessage

        viewModelScope.launch {
            try {
                val historyToSend = _messages.value.toMutableList()
                if (text.isBlank() && image != null) {
                    historyToSend[historyToSend.lastIndex] = userMessage.copy(text = "Analyze this image efficiently check for diseases and provide remedies")
                }
                val reply = repository.sendMessage(historyToSend)

                val aiMessage = ChatMessage(
                    text = reply,
                    isFromUser = false,
                    timestamp = "Now"
                )

                _messages.value = _messages.value + aiMessage
            } catch (e: Throwable) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.e("AIChatViewModel", "Error sending message", e)
                }
                val errorText = when {
                    e is retrofit2.HttpException -> "Server Error (${e.code()}): ${e.message()}"
                    e is java.io.IOException -> "Network Error: Please check your connection."
                    e is OutOfMemoryError -> "Error: Image too large (Out of Memory)."
                    else -> "Error: ${e.localizedMessage ?: "Unknown error"}"
                }
                
                val errorMessage = ChatMessage(errorText, false, "Now")
                _messages.value = _messages.value + errorMessage
            }
        }
    }
}

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String,
    val image: android.graphics.Bitmap? = null
)
