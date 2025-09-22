// screens/NotificationsScreen.kt
package com.bharatkrishi.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val notifications = listOf(
        NotificationItem(
            id = 1,
            title = "Weather Alert",
            message = "Heavy rain expected in 2 days. Cover your crops!",
            time = "2 hours ago",
            icon = Icons.Default.Warning,
            iconColor = Color(0xFFFF9800),
            isRead = false
        ),
        NotificationItem(
            id = 2,
            title = "Pest Identified",
            message = "Aphids detected on tomatoes. Immediate action required.",
            time = "5 hours ago",
            icon = Icons.Default.BugReport,
            iconColor = Color(0xFFf44336),
            isRead = false
        ),
        NotificationItem(
            id = 3,
            title = "Market Price Update",
            message = "Wheat prices increased by 2.4% today",
            time = "1 day ago",
            icon = Icons.Default.TrendingUp,
            iconColor = Color(0xFF4CAF50),
            isRead = true
        ),
        NotificationItem(
            id = 4,
            title = "Fertilizer Reminder",
            message = "Time to apply nitrogen fertilizer to your wheat crop",
            time = "2 days ago",
            icon = Icons.Default.Grass,
            iconColor = Color(0xFF2196F3),
            isRead = true
        ),
        NotificationItem(
            id = 5,
            title = "Soil Test Results",
            message = "Your soil test results are ready. pH level: 6.5",
            time = "3 days ago",
            icon = Icons.Default.Science,
            iconColor = Color(0xFF9C27B0),
            isRead = true
        ),
        NotificationItem(
            id = 6,
            title = "New Tutorial Available",
            message = "Learn about organic farming techniques",
            time = "1 week ago",
            icon = Icons.Default.PlayCircle,
            iconColor = Color(0xFF607D8B),
            isRead = true
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Notifications",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(onClick = { /* Mark all as read */ }) {
                    Text(
                        "Mark all read",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // Notification stats
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "${notifications.count { !it.isRead }} unread notifications",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        "Stay updated with important farm alerts",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Notifications List
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                NotificationCard(notification = notification)
            }

            // Add bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFF3E5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(notification.iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    notification.icon,
                    contentDescription = notification.title,
                    tint = notification.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Notification content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2196F3))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    notification.message,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    notification.time,
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val iconColor: Color,
    val isRead: Boolean
)