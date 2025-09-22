package com.bharatkrishi.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropAdvisoryScreen(navController: NavController) {
    val cropRecommendations = listOf(
        CropRecommendation(
            name = "Wheat",
            suitability = "Excellent for winter season",
            marketInfo = "High demand, good price",
            color = Color(0xFF4CAF50),
            season = "Rabi Season"
        ),
        CropRecommendation(
            name = "Cotton",
            suitability = "Suitable for your soil type",
            marketInfo = "Premium variety available",
            color = Color(0xFF2196F3),
            season = "Kharif Season"
        ),
        CropRecommendation(
            name = "Sugarcane",
            suitability = "Good irrigation available",
            marketInfo = "Processing unit nearby",
            color = Color(0xFFFF9800),
            season = "Year Round"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Crop Advisory", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Crop Recommendations",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Based on your farm history, soil type, and market trends",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            items(cropRecommendations) { crop ->
                CropRecommendationCard(crop)
            }
        }
    }
}

@Composable
fun CropRecommendationCard(crop: CropRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(crop.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Agriculture,
                    contentDescription = crop.name,
                    tint = crop.color,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        crop.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(crop.season, fontSize = 10.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = crop.color.copy(alpha = 0.2f)
                        )
                    )
                }

                Text(
                    crop.suitability,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    crop.marketInfo,
                    fontSize = 12.sp,
                    color = crop.color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

data class CropRecommendation(
    val name: String,
    val suitability: String,
    val marketInfo: String,
    val color: Color,
    val season: String
)

