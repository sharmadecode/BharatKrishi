package com.bharatkrishi.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bharatkrishi.app.MarketViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.text
import androidx.compose.ui.unit.dp
import com.bharatkrishi.app.WeatherViewModel
import com.bharatkrishi.app.network.NetworkResponse


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, marketViewModel: MarketViewModel, weatherViewModel: WeatherViewModel) {
    var selectedLanguage by remember { mutableStateOf("English") }
    var expanded by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("Rajesh Kumar") }
    val weatherResult by weatherViewModel.weatherResult.observeAsState()

    LaunchedEffect(Unit) {
        weatherViewModel.getData("New Delhi")
    }


    val languages = listOf("English", "Hindi", "Punjabi", "Tamil", "Telugu", "Bengali")

    val quickActions = listOf(
        QuickAction("Soil Analysis", Icons.Default.Biotech, "soil_info"),
        QuickAction("AI Assistant", Icons.Default.SmartToy, "ai_chat"),
        QuickAction("Pest Control", Icons.Default.BugReport, "pest_control"),
        QuickAction("Fertilizer Guide", Icons.Default.Grass, "fertilizer_advisory")
    )

    // Use a Scaffold for a standard screen layout (TopBar, Content, BottomBar)
    Scaffold(
        topBar = {
            // Top Bar
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.navigate("youtube_tutorials") }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                        Text(
                            "BharatKrishi",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        IconButton(onClick = { navController.navigate("notifications") }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding -> // This innerPadding is provided by the Scaffold
        // This Column now correctly holds only the scrollable content
        Column(
            modifier = Modifier
                .padding(innerPadding) // Apply padding from Scaffold
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp), // Add your own content padding
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Good Morning, $username",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedLanguage,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .width(150.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            languages.forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language) },
                                    onClick = {
                                        selectedLanguage = language
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Market Prices
            Box(
                modifier = Modifier.clickable {
                    // This is the action that will happen on click
                    navController.navigate("market_prices")
                }
            ) {
                MarketPricePreview(marketViewModel = marketViewModel)
            }

            // Weather Alert
            when (val result = weatherResult) {
                is NetworkResponse.Success -> {
                    WeatherAlertCard(
                        temperature = "${result.data.current.temp_c.toInt()}°C",
                        description = result.data.current.condition.text,
                        onClick = { navController.navigate("weather_page") }
                    )
                }
                is NetworkResponse.Loading -> {
                    WeatherAlertCard(
                        temperature = "--°C",
                        description = "Loading weather...",
                        onClick = { /* Do nothing while loading */ }
                    )
                }
                is NetworkResponse.Error -> {
                    WeatherAlertCard(
                        temperature = "!",
                        description = "Could not load weather",
                        onClick = { weatherViewModel.getData("Delhi") } // Allow retry
                    )
                }
                null -> {
                    WeatherAlertCard(
                        temperature = "--°C",
                        description = "Fetching weather...",
                        onClick = { /* Do nothing */ }
                    )
                }
            }

            // Quick Actions in 2x2 Grid
            Text(
                "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            QuickActionsGrid(navController, quickActions)
        }
    }
}

@Composable
fun QuickActionsGrid(navController: NavController, quickActions: List<QuickAction>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionCard(
                quickActions[0],
                Modifier
                    .weight(1f)
                    .height(100.dp)
            ) { navController.navigate(quickActions[0].route) }
            QuickActionCard(
                quickActions[1],
                Modifier
                    .weight(1f)
                    .height(100.dp)
            ) { navController.navigate(quickActions[1].route) }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionCard(
                quickActions[2],
                Modifier
                    .weight(1f)
                    .height(100.dp)
            ) { navController.navigate(quickActions[2].route) }
            QuickActionCard(
                quickActions[3],
                Modifier
                    .weight(1f)
                    .height(100.dp)
            ) { navController.navigate(quickActions[3].route) }
        }
    }
}

@Composable
fun QuickActionCard(action: QuickAction, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                action.icon,
                contentDescription = action.title,
                modifier = Modifier.size(30.dp),
                tint = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                action.title,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun WeatherAlertCard(
    temperature: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Cloud,
                contentDescription = "Weather",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    "Today's Weather",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "$temperature • $description",
                    fontSize = 14.sp,
                    color = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(
        containerColor = Color.White
    ) {
        val items = listOf(
            BottomNavItem("Home", Icons.Default.Home, "home"),
            BottomNavItem("Advisory", Icons.Default.Agriculture, "crop_advisory"),
            BottomNavItem("Help", Icons.Default.Help, "help_support"),
            BottomNavItem("Profile", Icons.Default.Person, "profile")
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title, fontSize = 10.sp) },
                selected = item.route == "home",
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}

// Data classes
data class QuickAction(val title: String, val icon: ImageVector, val route: String)
data class BottomNavItem(val title: String, val icon: ImageVector, val route: String)
