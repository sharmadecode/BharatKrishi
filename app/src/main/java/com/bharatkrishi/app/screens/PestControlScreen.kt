package com.bharatkrishi.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
fun PestControlScreen(navController: NavController) {
    val pests = listOf(
        PestInfo(
            name = "Aphids",
            description = "Small insects that suck plant sap",
            severity = "High",
            severityColor = Color(0xFFf44336)
        ),
        PestInfo(
            name = "Whitefly",
            description = "Causes yellowing of leaves",
            severity = "Medium",
            severityColor = Color(0xFFFF9800)
        ),
        PestInfo(
            name = "Bollworm",
            description = "Affects cotton crops severely",
            severity = "High",
            severityColor = Color(0xFFf44336)
        ),
        PestInfo(
            name = "Stem Borer",
            description = "Common in rice crops",
            severity = "Medium",
            severityColor = Color(0xFFFF9800)
        ),
        PestInfo(
            name = "Leaf Miner",
            description = "Creates tunnels in leaves",
            severity = "Low",
            severityColor = Color(0xFF4CAF50)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Pest Control Guide", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pests) { pest ->
                PestCard(pest)
            }
        }
    }
}

@Composable
fun PestCard(pest: PestInfo) {
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
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(pest.severityColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pest.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    pest.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Text(
                "${pest.severity} Risk",
                fontSize = 12.sp,
                color = pest.severityColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class PestInfo(
    val name: String,
    val description: String,
    val severity: String,
    val severityColor: Color
)
