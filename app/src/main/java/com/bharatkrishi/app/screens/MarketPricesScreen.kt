package com.bharatkrishi.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPricesScreen(navController: NavController) {
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Grains", "Vegetables", "Fruits", "Cash Crops")

    val marketData = listOf(
        MarketCrop("Wheat", "₹2,150", "₹2,100", "+2.4%", Color(0xFF4CAF50), "Grains"),
        MarketCrop("Rice (Basmati)", "₹4,200", "₹4,150", "+1.2%", Color(0xFF4CAF50), "Grains"),
        MarketCrop("Cotton", "₹5,670", "₹5,800", "-2.2%", Color(0xFFf44336), "Cash Crops"),
        MarketCrop("Sugarcane", "₹350", "₹340", "+2.9%", Color(0xFF4CAF50), "Cash Crops"),
        MarketCrop("Tomato", "₹25", "₹30", "-16.7%", Color(0xFFf44336), "Vegetables"),
        MarketCrop("Onion", "₹18", "₹22", "-18.2%", Color(0xFFf44336), "Vegetables"),
        MarketCrop("Potato", "₹12", "₹15", "-20.0%", Color(0xFFf44336), "Vegetables"),
        MarketCrop("Apple", "₹80", "₹75", "+6.7%", Color(0xFF4CAF50), "Fruits"),
        MarketCrop("Banana", "₹35", "₹38", "-7.9%", Color(0xFFf44336), "Fruits"),
        MarketCrop("Mango", "₹45", "₹40", "+12.5%", Color(0xFF4CAF50), "Fruits")
    )

    val filteredData = if (selectedCategory == "All") {
        marketData
    } else {
        marketData.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Market Prices", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* Refresh action */ }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Market Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = "Market Trend",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Last Updated: Today, 2:30 PM",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Text(
                            "Prices may vary by location and quality",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Category Filter
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                modifier = Modifier.padding(bottom = 16.dp),
                containerColor = Color.Transparent,
                edgePadding = 0.dp
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selectedCategory == category)
                                    Color(0xFF2E7D32)
                                else
                                    Color.White
                            )
                    ) {
                        Text(
                            category,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (selectedCategory == category) Color.White else Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Market Prices List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredData) { crop ->
                    MarketCropCard(crop)
                }
            }
        }
    }
}

@Composable
fun MarketCropCard(crop: MarketCrop) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    crop.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "per quintal",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    crop.currentPrice,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Yesterday: ${crop.previousPrice}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Price change indicator
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = crop.changeColor.copy(alpha = 0.1f)
                ),
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (crop.changePercentage.startsWith("+"))
                            Icons.Default.TrendingUp
                        else
                            Icons.Default.TrendingDown,
                        contentDescription = "Trend",
                        tint = crop.changeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        crop.changePercentage,
                        color = crop.changeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class MarketCrop(
    val name: String,
    val currentPrice: String,
    val previousPrice: String,
    val changePercentage: String,
    val changeColor: Color,
    val category: String
)