package com.bharatkrishi.app.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bharatkrishi.app.WeatherViewModel
import com.bharatkrishi.app.network.NetworkResponse
import com.bharatkrishi.app.network.WeatherModel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.LazyColumn


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherForecastScreen(navController: NavController, viewModel: WeatherViewModel) {var city by remember {
    mutableStateOf("")
}
    val weatherResult by viewModel.weatherResult.observeAsState()

    // Use a Scaffold for a standard screen layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Forecast") },

                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7D91EF), // Set the background color
                    titleContentColor = Color.White,       // Set the title text color
                    navigationIconContentColor = Color.White // Set the back arrow color
                )

            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding) // Apply the padding from the Scaffold
                .fillMaxSize() // Use fillMaxSize to take up the whole screen
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search Bar (This part is correct)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(text = "Search for any location") }
                )
                IconButton(onClick = { viewModel.getData(city) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search for any location"
                    )
                }
            }

            // Display UI based on state (This part is correct)
            when (val result = weatherResult) {
                is NetworkResponse.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = result.message,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is NetworkResponse.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is NetworkResponse.Success -> {
                    // You have a layout issue here where WeatherDetails is too big.
                    // This should be wrapped in a LazyColumn as suggested before.
                    WeatherDetails(data = result.data)
                }

                null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Search for a city to get the weather forecast.",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherDetails(data: WeatherModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location Icon",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Cyan

                    )
                    Text(text = data.location.name, fontSize = 30.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = data.location.country, fontSize = 18.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${data.current.temp_c}°c",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                AsyncImage(
                    modifier = Modifier.size(160.dp),
                    model = "https:${data.current.condition.icon}".replace("64x64", "128x128"),
                    contentDescription = "Condition Icon",
                )
                Text(
                    text = data.current.condition.text,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF879AEF)
                    )

                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    )
                    {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            WeatherKeyValue("Humidity", "${data.current.humidity}%")
                            WeatherKeyValue("Feels Like", "${data.current.feelslike_c}°c")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            WeatherKeyValue("UV", data.current.uv.toString())
                            WeatherKeyValue("Wind Speed", "${data.current.wind_kph} km/h")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            WeatherKeyValue("Local Time", data.location.localtime.split(" ")[1])
                            WeatherKeyValue("Local Date", data.location.localtime.split(" ")[0])
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun WeatherKeyValue(key: String, value: String) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = key, fontWeight = FontWeight.SemiBold, color = Color.DarkGray )
    }
}