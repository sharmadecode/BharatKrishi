// screens/ProfileScreen.kt
package com.bharatkrishi.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var isEditMode by remember { mutableStateOf(false) }
    var farmerName by remember { mutableStateOf("Rajesh Kumar") }
    var age by remember { mutableStateOf("45") }
    var location by remember { mutableStateOf("Hyderabad, Telangana") }
    var farmSize by remember { mutableStateOf("5 acres") }
    var farmerType by remember { mutableStateOf("Small Scale Farmer") }
    var primaryCrops by remember { mutableStateOf("Rice, Cotton, Wheat") }
    var soilType by remember { mutableStateOf("Black Cotton Soil") }
    var experience by remember { mutableStateOf("20 years") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Profile", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(
                    onClick = { isEditMode = !isEditMode }
                ) {
                    Text(
                        if (isEditMode) "Save" else "Edit",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Profile Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Picture
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(4.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(60.dp),
                                tint = Color(0xFF2E7D32)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isEditMode) {
                            OutlinedTextField(
                                value = farmerName,
                                onValueChange = { farmerName = it },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        } else {
                            Text(
                                farmerName,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            farmerType,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                location,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                // Basic Information
                Text(
                    "Basic Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ProfileField("Age", age, Icons.Default.Cake, isEditMode) { age = it }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileField("Experience", experience, Icons.Default.Timeline, isEditMode) { experience = it }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileField("Location", location, Icons.Default.LocationOn, isEditMode) { location = it }
                    }
                }
            }

            item {
                // Farm Details
                Text(
                    "Farm Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ProfileField("Farm Size", farmSize, Icons.Default.Landscape, isEditMode) { farmSize = it }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileField("Soil Type", soilType, Icons.Default.Terrain, isEditMode) { soilType = it }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileField("Primary Crops", primaryCrops, Icons.Default.Agriculture, isEditMode) { primaryCrops = it }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileField("Farmer Type", farmerType, Icons.Default.Category, isEditMode) { farmerType = it }
                    }
                }
            }

            item {
                // Statistics
                Text(
                    "Farm Statistics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        "Crops Grown",
                        "12",
                        Icons.Default.Agriculture,
                        Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        "Total Yield",
                        "45 quintals",
                        Icons.Default.BarChart,
                        Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    StatCard(
                        "Soil Tests",
                        "8",
                        Icons.Default.Science,
                        Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        "AI Scans",
                        "25",
                        Icons.Default.SmartToy,
                        Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                // Account Settings
                Text(
                    "Account Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        SettingsItem("Change Password", Icons.Default.Lock)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingsItem("Notification Settings", Icons.Default.Notifications)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingsItem("Language Settings", Icons.Default.Language)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingsItem("Privacy Policy", Icons.Default.PrivacyTip)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingsItem("Help & Support", Icons.Default.Help)
                    }
                }
            }

            item {
                // Logout Button
                Button(
                    onClick = { /* Logout logic */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFf44336)
                    )
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    icon: ImageVector,
    isEditMode: Boolean,
    onValueChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                label,
                fontSize = 12.sp,
                color = Color.Gray
            )

            if (isEditMode) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = label != "Primary Crops"
                )
            } else {
                Text(
                    value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                title,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SettingsItem(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            title,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
