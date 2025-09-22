package com.bharatkrishi.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerAdvisoryScreen(navController: NavController) {
    val fertilizerRecommendations = listOf(
        FertilizerRecommendation(
            cropName = "Wheat",
            stage = "Sowing Stage",
            fertilizer = "DAP (Diammonium Phosphate)",
            quantity = "100 kg/hectare",
            timing = "At the time of sowing",
            description = "Provides phosphorus for strong root development"
        ),
        FertilizerRecommendation(
            cropName = "Rice",
            stage = "Vegetative Stage",
            fertilizer = "Urea",
            quantity = "120 kg/hectare",
            timing = "20-25 days after transplanting",
            description = "Essential nitrogen for leaf growth"
        ),
        FertilizerRecommendation(
            cropName = "Cotton",
            stage = "Flowering Stage",
            fertilizer = "Potash",
            quantity = "60 kg/hectare",
            timing = "During flower initiation",
            description = "Improves fiber quality and yield"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Fertilizer Advisory", fontWeight = FontWeight.Bold) },
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
            items(fertilizerRecommendations) { recommendation ->
                FertilizerCard(recommendation)
            }
        }
    }
}

@Composable
fun FertilizerCard(recommendation: FertilizerRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    recommendation.cropName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    onClick = { },
                    label = { Text(recommendation.stage, fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF2E7D32).copy(alpha = 0.1f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FertilizerDetailRow("Fertilizer", recommendation.fertilizer, Icons.Default.Science)
            FertilizerDetailRow("Quantity", recommendation.quantity, Icons.Default.Scale)
            FertilizerDetailRow("Timing", recommendation.timing, Icons.Default.Schedule)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                recommendation.description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun FertilizerDetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "$label: ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

data class FertilizerRecommendation(
    val cropName: String,
    val stage: String,
    val fertilizer: String,
    val quantity: String,
    val timing: String,
    val description: String
)
