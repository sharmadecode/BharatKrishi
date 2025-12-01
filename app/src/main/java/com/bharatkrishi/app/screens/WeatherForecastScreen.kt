// screens/WeatherForecastScreen.kt
package com.bharatkrishi.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherForecastScreen(navController: NavController) {
    val currentWeather = WeatherData(
        temperature = 28,
        condition = "Partly Cloudy",
        humidity = 65,
        windSpeed = 12,
        uvIndex = 6,
        icon = Icons.Default.WbCloudy   // ✅ fixed
    )

    val hourlyForecast = listOf(
        HourlyWeather("12 PM", 28, Icons.Default.WbSunny),
        HourlyWeather("1 PM", 30, Icons.Default.WbSunny),
        HourlyWeather("2 PM", 32, Icons.Default.WbCloudy),   // ✅ fixed
        HourlyWeather("3 PM", 31, Icons.Default.Cloud),
        HourlyWeather("4 PM", 29, Icons.Default.Cloud),
        HourlyWeather("5 PM", 27, Icons.Default.AcUnit)      // ✅ fixed
    )

    val weeklyForecast = listOf(
        DailyWeather("Today", 32, 24, "Partly Cloudy", Icons.Default.WbCloudy, 20),
        DailyWeather("Tomorrow", 29, 22, "Light Rain", Icons.Default.Opacity, 80),  // ✅ rain
        DailyWeather("Friday", 26, 20, "Heavy Rain", Icons.Default.Opacity, 90),    // ✅ rain
        DailyWeather("Saturday", 25, 19, "Rainy", Icons.Default.Opacity, 85),       // ✅ rain
        DailyWeather("Sunday", 28, 21, "Cloudy", Icons.Default.Cloud, 40),
        DailyWeather("Monday", 30, 23, "Sunny", Icons.Default.WbSunny, 10),
        DailyWeather("Tuesday", 33, 25, "Hot", Icons.Default.WbSunny, 5)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Weather Forecast", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* Refresh weather */ }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
                // Current Weather Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Hyderabad, Telangana",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                currentWeather.icon,
                                contentDescription = currentWeather.condition,
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    "${currentWeather.temperature}°C",
                                    color = Color.White,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Light
                                )
                                Text(
                                    currentWeather.condition,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Weather Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WeatherDetailItem("Humidity", "${currentWeather.humidity}%", Icons.Default.Water)
                            WeatherDetailItem("Wind", "${currentWeather.windSpeed} km/h", Icons.Default.Air)
                            WeatherDetailItem("UV Index", "${currentWeather.uvIndex}", Icons.Default.WbSunny)
                        }
                    }
                }
            }

            item {
                // Weather Alert
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Weather Alert",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                "Heavy rain expected in 2 days. Protect your crops!",
                                fontSize = 14.sp,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }
            }

            item {
                // Hourly Forecast
                Text(
                    "Hourly Forecast",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(hourlyForecast) { hourly ->
                        HourlyWeatherCard(hourly)
                    }
                }
            }

            item {
                // 7-Day Forecast
                Text(
                    "7-Day Forecast",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(weeklyForecast) { daily ->
                DailyWeatherCard(daily)
            }

            item {
                // Farming Tips based on weather
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = "Tips",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Weather-Based Farming Tips",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val tips = listOf(
                            "Cover crops before expected rainfall",
                            "Good time for transplanting after rain",
                            "Monitor for fungal diseases in humid conditions",
                            "Ensure proper drainage in fields"
                        )

                        tips.forEach { tip ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text("•", color = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(tip, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetailItem(title: String, value: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            title,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun HourlyWeatherCard(hourly: HourlyWeather) {
    Card(
        modifier = Modifier.width(80.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                hourly.time,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                hourly.icon,
                contentDescription = "Weather",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${hourly.temperature}°",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DailyWeatherCard(daily: DailyWeather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                daily.day,
                modifier = Modifier.width(80.dp),
                fontWeight = FontWeight.Medium
            )

            Icon(
                daily.icon,
                contentDescription = daily.condition,
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    daily.condition,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${daily.rainChance}% chance of rain",
                    fontSize = 12.sp,
                    color = if (daily.rainChance > 50) Color(0xFF1976D2) else Color.Gray
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "${daily.maxTemp}°",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${daily.minTemp}°",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// Data classes
data class WeatherData(
    val temperature: Int,
    val condition: String,
    val humidity: Int,
    val windSpeed: Int,
    val uvIndex: Int,
    val icon: ImageVector
)

data class HourlyWeather(
    val time: String,
    val temperature: Int,
    val icon: ImageVector
)

data class DailyWeather(
    val day: String,
    val maxTemp: Int,
    val minTemp: Int,
    val condition: String,
    val icon: ImageVector,
    val rainChance: Int
)

