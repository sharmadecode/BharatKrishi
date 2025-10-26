package com.bharatkrishi.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatkrishi.app.DataState
import com.bharatkrishi.app.MarketViewModel
import com.bharatkrishi.app.MarketData


@Composable
fun MarketPricePreview(marketViewModel: MarketViewModel) {
    // Observe the data state from the ViewModel
    val dataState by marketViewModel.marketDataState.observeAsState()

    Column {
        Text(
            text = "Market Prices",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (val state = dataState) {
            is DataState.Loading -> {
                // Show a small loading indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }

            is DataState.Success -> {
                // Show the first 3 items horizontally
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Take only the first 3 items to show as a preview
                    state.data.take(3).forEach { marketData ->
                        Box(modifier = Modifier.weight(1f)) {
                            PreviewCropCard(data = marketData)
                        }
                    }
                }
            }

            is DataState.Error -> {
                // Show an error message
                Text(text = "Could not load prices.", color = Color.Red)
            }

            null -> {
                // Initial state
                Text(text = "Loading prices...")
            }
        }
    }
}

@Composable
fun PreviewCropCard(data: MarketData) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF)) // Light blueish color
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = data.commodity ?: "N/A",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "₹${data.modal_price}",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}