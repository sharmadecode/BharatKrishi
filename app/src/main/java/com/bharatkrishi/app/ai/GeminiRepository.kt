package com.bharatkrishi.app.ai

import com.bharatkrishi.app.BuildConfig
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

class GeminiRepository(
    private val api: GeminiApiService
) {
    private val systemPrompt = """
        You are "Krishi Mitra", a friendly, expert agricultural AI assistant inside the BharatKrishi app.
        You help Indian farmers by giving simple, accurate, practical advice in Hindi or Hinglish.
        Tone: Supportive, humble, like a village farming advisor.

        Responsibilities:
        - Wheat disease detection (Yellow/Brown/Stripe rust, Loose smut, Septoria, Mildew).
        - Drone field analysis & Soil health.
        - Weather-based advisory & Market updates.
        - Pest & fertilizer guidance (Safe, govt-approved only).

        Voice Instructions:
        - Respond as if speaking Hindi.
        - Keep sentences clean for Text-to-Speech.
        - Avoid English words unless necessary.

        Creator: If asked who created this app, respond with "ADITYA RAKESH SHARMA".
        Never give illegal/harmful advice or medical advice.
    """.trimIndent()

    suspend fun sendMessage(history: List<com.bharatkrishi.app.ChatMessage>): String {
        val contents = history.map { message ->
            val parts = mutableListOf<GeminiPart>()
            parts.add(GeminiPart(text = message.text))
            if (message.image != null) {
                val base64Image = bitmapToBase64(message.image)
                parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64Image)))
            }
            GeminiContent(
                role = if (message.isFromUser) "user" else "model",
                parts = parts
            )
        }

        val request = GeminiGenerateContentRequest(
            contents = contents,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            )
        )

        val response = try {
            retryIO {
                api.generateContent(
                    request = request
                )
            }
        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) {
                android.util.Log.e("GeminiRepository", "Error generating content", e)
            }
            return when (e) {
                is HttpException -> {
                    if (e.code() == 429) "System busy. Please wait a moment." 
                    else "Server is currently unreachable. Please try again later."
                }
                is IOException -> "Network error. Please check your internet connection."
                else -> "An error occurred. Please try again."
            }
        }

        return response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: "Maaf kijiye, samajh nahi aaya."
    }

    private suspend fun bitmapToBase64(bitmap: android.graphics.Bitmap): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val byteArrayOutputStream = java.io.ByteArrayOutputStream()
        
        val scaledBitmap = if (bitmap.width > 512 || bitmap.height > 512) {
             val scale = 512.0f / kotlin.math.max(bitmap.width, bitmap.height)
             android.graphics.Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else {
             bitmap
        }
        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
    }

    private suspend fun <T> retryIO(
        times: Int = 2,
        initialDelay: Long = 1000, 
        maxDelay: Long = 10000,    
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) {
            try {
                return block()
            } catch (e: Exception) {
                
                val shouldRetry = e is IOException || (e is HttpException && (e.code() == 429 || e.code() in 500..599))
                if (!shouldRetry) throw e
                
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
        return block() 
    }
}
