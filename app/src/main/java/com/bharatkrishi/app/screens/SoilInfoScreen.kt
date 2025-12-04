package com.bharatkrishi.app.screens

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer

// 8-class wheat disease labels
private val CLASS_LABELS = arrayOf(
    "Healthy",
    "Yellow rust",
    "Brown rust",
    "Loose Smut",
    "Unknown",
    "Septoria",
    "Stripe rust",
    "Mildew"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilInfoScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf("Crop Scanner") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Soil & Crop Analysis", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // Tab Row
        TabRow(
            selectedTabIndex = if (selectedTab == "Crop Scanner") 0 else 1,
            containerColor = Color.White
        ) {
            Tab(
                selected = selectedTab == "Crop Scanner",
                onClick = { selectedTab = "Crop Scanner" },
                text = { Text("Crop Scanner") }
            )
            Tab(
                selected = selectedTab == "Soil Analysis",
                onClick = { selectedTab = "Soil Analysis" },
                text = { Text("Soil Analysis") }
            )
        }

        when (selectedTab) {
            "Crop Scanner" -> CropScannerContent()
            "Soil Analysis" -> SoilAnalysisContent()
        }
    }
}

@Composable
fun CropScannerContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var ortEnvironment by remember { mutableStateOf<OrtEnvironment?>(null) }
    var ortSession by remember { mutableStateOf<OrtSession?>(null) }
    var analysisResult by remember { mutableStateOf<String?>(null) }
    var modelError by remember { mutableStateOf<String?>(null) }

    // CAMERA permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            analysisResult = "Camera permission denied."
        }
    }

    // CAMERA launcher (uses preview bitmap)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null && ortSession != null && ortEnvironment != null) {
            analysisResult = runModelOnBitmap(bitmap, ortSession!!, ortEnvironment!!)
        } else if (bitmap == null) {
            analysisResult = "Failed to capture image."
        } else {
            analysisResult = "Model is not loaded yet."
        }
    }

    // Load ONNX model (runs once)
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val env = OrtEnvironment.getEnvironment()
                ortEnvironment = env

                val modelFile = loadOnnxModel(context, "mobilevit_wheat_8class.onnx")
                ortSession = env.createSession(modelFile.absolutePath)

                Log.d("ONNX", "Model loaded successfully")
            } catch (e: Exception) {
                Log.e("ONNX", "Error loading ONNX model", e)
                modelError = "Failed to load model: ${e.message}"
            }
        }
    }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scanner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF2E7D32)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Scan Your Crop",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        "Take a photo of your crop leaves to detect diseases, pests, and nutrient deficiencies",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // Check camera permission at runtime
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (!hasPermission) {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            } else {
                                cameraLauncher.launch(null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        )
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Camera & Analyze")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            // TODO: implement gallery picker later if needed
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select from Gallery (coming soon)")
                    }

                    // Model loading status
                    Spacer(modifier = Modifier.height(8.dp))
                    if (ortSession == null && modelError == null) {
                        Text(
                            "Loading AI model...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    modelError?.let {
                        Text(
                            it,
                            fontSize = 12.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }

        // Result card
        analysisResult?.let { result ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Prediction",
                                tint = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AI Diagnosis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            result,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // How it works
        item {
            Text(
                "How it works",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            val steps = listOf(
                ScanStep("1", "Take Photo", "Capture clear image of affected crop leaves", Icons.Default.Camera),
                ScanStep("2", "AI Analysis", "Our AI analyzes the image for diseases and pests", Icons.Default.AutoAwesome),
                ScanStep("3", "Get Results", "Receive diagnosis with treatment recommendations", Icons.Default.Assignment)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.forEach { step ->
                    StepCard(step)
                }
            }
        }

        // Recent Scans (static demo data)
        item {
            Text(
                "Recent Scans",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            val recentScans = listOf(
                ScanResult("Tomato Leaves", "Healthy", "2 hours ago", Color(0xFF4CAF50)),
                ScanResult("Wheat Crop", "Early Blight Detected", "1 day ago", Color(0xFFFF9800)),
                ScanResult("Cotton Plant", "Aphid Infestation", "3 days ago", Color(0xFFf44336))
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recentScans) { scan ->
                    RecentScanCard(scan)
                }
            }
        }
    }
}

// Copy ONNX model from assets to internal storage
fun loadOnnxModel(context: Context, modelName: String): File {
    val onnxFile = File(context.filesDir, modelName)

    if (!onnxFile.exists()) {
        context.assets.open(modelName).use { input ->
            FileOutputStream(onnxFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    // If you ALSO have a .onnx.data, you can copy it similarly here.
    // If not, ONNXRuntime may still try to look for it but will log only.

    return onnxFile
}

// Run model on captured bitmap
fun runModelOnBitmap(
    bitmap: Bitmap,
    session: OrtSession,
    env: OrtEnvironment
): String {
    return try {
        // 1. Resize to 256×256
        val resized = Bitmap.createScaledBitmap(bitmap, 256, 256, true)

        // 2. Convert bitmap → FloatArray (CHW format)
        val input = FloatArray(1 * 3 * 256 * 256)
        val width = 256
        val height = 256

        var idx = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = resized.getPixel(x, y)

                val r = ((pixel shr 16) and 0xFF) / 255f
                val g = ((pixel shr 8) and 0xFF) / 255f
                val b = (pixel and 0xFF) / 255f

                // CHW layout: all R first, then G, then B
                input[idx] = r
                input[idx + width * height] = g
                input[idx + 2 * width * height] = b

                idx++
            }
        }

        val shape = longArrayOf(1, 3, 256, 256)
        val tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(input), shape)

        val inputName = session.inputNames.first()
        val output = session.run(mapOf(inputName to tensor))[0].value

        val scores: FloatArray = when (output) {
            is FloatArray -> output
            is Array<*> -> output[0] as FloatArray
            else -> throw Exception("Unknown output format: ${output!!::class.java}")
        }

        val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: 0
        val label = CLASS_LABELS.getOrNull(maxIndex) ?: "Unknown"
        val conf = scores[maxIndex]

        "Prediction: $label\nConfidence: ${"%.2f".format(conf)}"
    } catch (e: Exception) {
        Log.e("ONNX", "Bitmap inference failed", e)
        "Error during inference: ${e.message}"
    }
}

@Composable
fun SoilAnalysisContent() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Soil Test Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Science,
                            contentDescription = "Soil Test",
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Latest Soil Test Results",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Updated 5 days ago",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        item {
            // Soil Parameters
            val soilParams = listOf(
                SoilParameter("pH Level", "6.5", "Slightly Acidic", Color(0xFF4CAF50)),
                SoilParameter("Nitrogen (N)", "Medium", "180 kg/ha", Color(0xFFFF9800)),
                SoilParameter("Phosphorus (P)", "High", "25 ppm", Color(0xFF4CAF50)),
                SoilParameter("Potassium (K)", "Low", "120 ppm", Color(0xFFf44336)),
                SoilParameter("Organic Matter", "2.8%", "Good", Color(0xFF4CAF50)),
                SoilParameter("Soil Moisture", "65%", "Optimal", Color(0xFF2196F3))
            )

            Text(
                "Soil Parameters",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            soilParams.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { param ->
                        SoilParameterCard(
                            param,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            // Recommendations
            Text(
                "Recommendations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val recommendations = listOf(
                "Add potassium-rich fertilizer to improve K levels",
                "Consider lime application to increase pH slightly",
                "Maintain current nitrogen management practices",
                "Good phosphorus levels - no action needed"
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    recommendations.forEach { recommendation ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Recommendation",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                recommendation,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            // Action Button
            Button(
                onClick = { /* Schedule new soil test */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                Icon(Icons.Default.Schedule, contentDescription = "Schedule")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedule New Soil Test")
            }
        }
    }
}

@Composable
fun StepCard(step: ScanStep) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2E7D32)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    step.number,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    step.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    step.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Icon(
                step.icon,
                contentDescription = step.title,
                tint = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
fun RecentScanCard(scan: ScanResult) {
    Card(
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                scan.cropName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Text(
                scan.result,
                fontSize = 12.sp,
                color = scan.statusColor,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                scan.time,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SoilParameterCard(
    parameter: SoilParameter,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                parameter.name,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Text(
                parameter.value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                parameter.status,
                fontSize = 12.sp,
                color = parameter.statusColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Data classes
data class ScanStep(
    val number: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class ScanResult(
    val cropName: String,
    val result: String,
    val time: String,
    val statusColor: Color
)

data class SoilParameter(
    val name: String,
    val value: String,
    val status: String,
    val statusColor: Color
)
