package com.bharatkrishi.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.bharatkrishi.app.WeatherViewModel
import com.bharatkrishi.app.network.NetworkResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    marketViewModel: MarketViewModel,
    weatherViewModel: WeatherViewModel
) {
    var selectedLanguage by remember { mutableStateOf("English") }
    var expanded by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("Rajesh Kumar") }

    // SAME AS YOUR PREVIOUS WORKING CODE
    val weatherResult by weatherViewModel.weatherResult.observeAsState()

    LaunchedEffect(Unit) {
        weatherViewModel.getData("New Delhi")
    }

    val languages = listOf("English", "Hindi", "Punjabi", "Tamil", "Telugu", "Bengali")

    val quickActions = listOf(
        QuickAction("Soil Analysis", Icons.Default.Biotech, "soil_info"),
        QuickAction("AI Assistant", Icons.Default.SmartToy, "ai_chat"),
        QuickAction("Pest Control", Icons.Default.BugReport, "pest_control"),
        QuickAction("Fertilizer Guide", Icons.Default.Grass, "fertilizer_advisory"),
        QuickAction("Drone Analysis", Icons.Default.AirplanemodeActive, "drone_analysis"),
        QuickAction("Community Forum", Icons.Default.People, "community_forum")
    )


    Scaffold(
        topBar = {
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
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // WELCOME CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                            modifier = Modifier.menuAnchor().width(150.dp),
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

            // MARKET PRICE PREVIEW
            Box(
                modifier = Modifier.clickable {
                    navController.navigate("market_prices")
                }
            ) {
                MarketPricePreview(marketViewModel)
            }

            // WEATHER BLOCK
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
                        onClick = {}
                    )
                }

                is NetworkResponse.Error -> {
                    WeatherAlertCard(
                        temperature = "!",
                        description = "Could not load weather",
                        onClick = { weatherViewModel.getData("Delhi") }
                    )
                }

                null -> {
                    WeatherAlertCard(
                        temperature = "--°C",
                        description = "Fetching weather...",
                        onClick = {}
                    )
                }
            }

            // QUICK ACTIONS
            Text(
                "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            QuickActionsGrid(navController, quickActions)
        }
    }
}

@Composable
fun QuickActionsGrid(navController: NavController, quickActions: List<QuickAction>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionCard(quickActions[0], Modifier.weight(1f)) {
                navController.navigate(quickActions[0].route)
            }
            QuickActionCard(quickActions[1], Modifier.weight(1f)) {
                navController.navigate(quickActions[1].route)
            }
        }
        // Row 2 (Drone + Community)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionCard(quickActions[4], Modifier.weight(1f)) {
                navController.navigate(quickActions[4].route)
            }
            QuickActionCard(quickActions[5], Modifier.weight(1f)) {
                navController.navigate(quickActions[5].route)
            }
        }
        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionCard(quickActions[2], Modifier.weight(1f)) {
                navController.navigate(quickActions[2].route)
            }
            QuickActionCard(quickActions[3], Modifier.weight(1f)) {
                navController.navigate(quickActions[3].route)
            }
        }


    }
}


@Composable
fun QuickActionCard(action: QuickAction, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(action.icon, contentDescription = action.title, tint = Color(0xFF2E7D32))
            Spacer(modifier = Modifier.height(8.dp))
            Text(action.title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun WeatherAlertCard(temperature: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF1976D2))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Today's Weather", fontWeight = FontWeight.Medium)
                Text("$temperature • $description", color = Color(0xFF1976D2))
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF1976D2))
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar(containerColor = Color.White) {
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

data class QuickAction(val title: String, val icon: ImageVector, val route: String)
data class BottomNavItem(val title: String, val icon: ImageVector, val route: String)
