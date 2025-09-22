package com.bharatkrishi.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BharatKrishiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BharatKrishiApp()
                }
            }
        }
    }
}

@Composable
fun BharatKrishiApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HomeScreen(navController) }
        composable("notifications") { NotificationsScreen(navController) }
        composable("market_prices") { MarketPricesScreen(navController) }
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