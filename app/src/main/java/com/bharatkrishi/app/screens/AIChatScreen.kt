package com.bharatkrishi.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(navController: NavController) {
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(getSampleMessages()) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SmartToy,
                            contentDescription = "AI Assistant",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "KrishiMitra AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Online",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(message)
            }
        }

        // Quick Suggestions (if no messages)
        if (messages.size <= 1) {
            QuickSuggestions { suggestion ->
                messageText = suggestion
            }
        }

        // Input Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Ask me anything about farming...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        cursorColor = Color(0xFF2E7D32)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            messages = messages + ChatMessage(
                                text = messageText,
                                isFromUser = true,
                                timestamp = "Now"
                            )

                            // Simulate AI response
                            val aiResponse = generateAIResponse(messageText)
                            messages = messages + ChatMessage(
                                text = aiResponse,
                                isFromUser = false,
                                timestamp = "Now"
                            )

                            messageText = ""

                            // Scroll to bottom
                            coroutineScope.launch {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = Color(0xFF2E7D32)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser)
            Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            // AI Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = "AI",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser)
                    Color(0xFF2E7D32) else Color.White
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    message.text,
                    color = if (message.isFromUser) Color.White else Color.Black,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    message.timestamp,
                    color = if (message.isFromUser)
                        Color.White.copy(alpha = 0.7f) else Color.Gray,
                    fontSize = 10.sp
                )
            }
        }

        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun QuickSuggestions(onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            "Quick Questions",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val suggestions = listOf(
            "What fertilizer should I use for wheat?",
            "How to prevent pest attacks?",
            "Best time to sow cotton?",
            "Organic farming techniques"
        )

        suggestions.forEach { suggestion ->
            SuggestionChip(
                onClick = { onSuggestionClick(suggestion) },
                label = { Text(suggestion, fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Color.White,
                    labelColor = Color(0xFF2E7D32)
                )
            )
        }
    }
}

fun getSampleMessages(): List<ChatMessage> {
    return listOf(
        ChatMessage(
            text = "Hello! I'm KrishiMitra, your AI farming assistant. I'm here to help you with all your agricultural questions. How can I assist you today?",
            isFromUser = false,
            timestamp = "Just now"
        )
    )
}

fun generateAIResponse(userMessage: String): String {
    // Simple response generation based on keywords
    return when {
        userMessage.contains("wheat", true) -> {
            "For wheat cultivation, I recommend:\n\n• Best sowing time: October-December\n• Fertilizer: Apply 120kg Nitrogen, 60kg Phosphorus, 40kg Potassium per hectare\n• Irrigation: 5-6 irrigations needed\n• Harvest: March-April\n\nWould you like more specific information about any aspect?"
        }
        userMessage.contains("pest", true) -> {
            "For pest management:\n\n• Regular monitoring is key\n• Use integrated pest management (IPM)\n• Neem-based organic pesticides are effective\n• Encourage beneficial insects\n• Crop rotation helps break pest cycles\n\nWhat specific pest are you dealing with?"
        }
        userMessage.contains("fertilizer", true) -> {
            "Fertilizer recommendations depend on:\n\n• Soil test results\n• Crop type and growth stage\n• Local conditions\n\nGenerally:\n• Use balanced NPK fertilizers\n• Add organic matter regularly\n• Consider micronutrients\n\nDo you have recent soil test results?"
        }
        userMessage.contains("organic", true) -> {
            "Organic farming practices:\n\n• Compost and vermicompost\n• Green manuring\n• Biological pest control\n• Crop rotation and intercropping\n• Reduced tillage\n\nOrganic farming improves soil health and reduces chemical dependency. Which aspect interests you most?"
        }
        else -> {
            "That's a great question! Based on my knowledge of farming practices, I'd suggest consulting with local agricultural experts for the most accurate advice specific to your region and conditions. \n\nCould you provide more details about your specific situation? For example:\n• What crop are you growing?\n• What's your location?\n• What specific challenge are you facing?"
        }
    }
}

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String
)