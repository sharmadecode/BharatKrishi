package com.bharatkrishi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bharatkrishi.app.screens.*
import com.bharatkrishi.app.ui.theme.BharatKrishiTheme


class MainActivity : ComponentActivity() {

    
    private val marketViewModel: MarketViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        

        setContent {
            BharatKrishiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    
                    BharatKrishiApp(marketViewModel, weatherViewModel, authViewModel)
                }
            }
        }
    }
}

@Composable
fun BharatKrishiApp(
    marketViewModel: MarketViewModel,
    weatherViewModel: WeatherViewModel,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.user.observeAsState()

    val startDestination = "home"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") { LoginScreen(navController, authViewModel) }
        composable("signup") { SignupScreen(navController, authViewModel) }
        composable("profile_completion") { ProfileCompletionScreen(navController, authViewModel) }
        composable("home") { HomeScreen(navController, marketViewModel, weatherViewModel, authViewModel) }
        composable("notifications") { NotificationsScreen(navController) }
        
        composable("market_prices") { MarketPricesScreen(navController, marketViewModel) }
        composable("crop_selection") { CropSelectionScreen(navController) }
        composable(
            "soil_info?autoLaunch={autoLaunch}",
            arguments = listOf(navArgument("autoLaunch") { defaultValue = false; type = NavType.BoolType })
        ) { backStackEntry ->
            val autoLaunch = backStackEntry.arguments?.getBoolean("autoLaunch") ?: false
            SoilInfoScreen(navController, autoLaunch) 
        }
        composable("soil_health") { SoilStatusScreen(navController) }
        composable("ai_chat") { AIChatScreen(navController) }
        composable("weather_page") { WeatherForecastScreen(navController, weatherViewModel) }
        composable("fertilizer_advisory") { FertilizerAdvisoryScreen(navController) }
        composable("crop_advisory") { CropAdvisoryScreen(navController) }
        composable("pest_control") { PestControlScreen(navController) }
        composable("help_support") { HelpSupportScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("youtube_tutorials") { YoutubeTutorialsScreen(navController) }
        composable("drone_analysis") {
            DroneAnalysisScreen(navController)
        }
        composable("community_forum") { CommunityForumScreen(navController) }
        composable("crop_registration") { CropRegistrationScreen(navController) }
        composable("disaster_report") { DisasterReportScreen(navController) }
        composable("surveillance_map") { SurveillanceMapScreen(navController) }

        composable(
            route = "scheme_detail/{schemeTitle}/{schemeDescription}",
            arguments = listOf(
                navArgument("schemeTitle") { type = NavType.StringType },
                navArgument("schemeDescription") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("schemeTitle")
            val description = backStackEntry.arguments?.getString("schemeDescription")

            SchemeDetailScreen(
                navController = navController,
                schemeTitle = title,
                schemeDescription = description
            )
        }
    }
}
