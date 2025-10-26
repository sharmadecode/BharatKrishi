package com.bharatkrishi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bharatkrishi.app.screens.*
import com.bharatkrishi.app.ui.theme.BharatKrishiTheme

// --- MERGED AND CORRECTED MAIN ACTIVITY ---
class MainActivity : ComponentActivity() {

    // 1. Get a reference to the ViewModel using the standard delegate for Compose activities
    private val marketViewModel: MarketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Start fetching data as soon as the activity is created
        marketViewModel.fetchMarketData()

        setContent {
            BharatKrishiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // You can pass the ViewModel down to your composable if needed
                    BharatKrishiApp(marketViewModel)
                }
            }
        }
    }
}

@Composable
fun BharatKrishiApp(marketViewModel: MarketViewModel) { // Pass the ViewModel here
    val navController = rememberNavController()

    // You can now observe the ViewModel's state here or pass it further down
    // to the specific screen that needs it (e.g., MarketPricesScreen).

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HomeScreen(navController, marketViewModel) }
        composable("notifications") { NotificationsScreen(navController) }
        // Example: Pass the ViewModel to the screen that will display the data
        composable("market_prices") { MarketPricesScreen(navController, marketViewModel) }
        composable("soil_info") { SoilInfoScreen(navController) }
        composable("ai_chat") { AIChatScreen(navController) }
        composable("weather_forecast") { WeatherForecastScreen(navController) }
        composable("fertilizer_advisory") { FertilizerAdvisoryScreen(navController) }
        composable("crop_advisory") { CropAdvisoryScreen(navController) }
        composable("pest_control") { PestControlScreen(navController) }
        composable("help_support") { HelpSupportScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("youtube_tutorials") { YoutubeTutorialsScreen(navController) }
    }
}
