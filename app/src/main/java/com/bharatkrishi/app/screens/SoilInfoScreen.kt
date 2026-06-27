package com.bharatkrishi.app.screens

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.util.Date
import java.text.SimpleDateFormat
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.asImageBitmap
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
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer
import com.bharatkrishi.app.utils.LocalizationManager
import com.bharatkrishi.app.data.FirebaseManager
import com.bharatkrishi.app.utils.GPSLocationManager
import com.google.firebase.auth.FirebaseAuth
import com.bharatkrishi.app.models.CropType
import com.bharatkrishi.app.models.CropPreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilInfoScreen(navController: NavController, autoLaunch: Boolean = false) {
    val context = LocalContext.current
    val prefManager = remember { CropPreferenceManager(context) }
    var selectedCrop by remember { mutableStateOf(prefManager.getSelectedCrop()) }
    val localRecentScans = remember {
        mutableStateListOf(
            ScanResult(LocalizationManager.get("Wheat"), LocalizationManager.get("Early Blight Detected"), LocalizationManager.get("5 days ago"), Color(0xFFFF9800)),
            ScanResult(LocalizationManager.get("Wheat"), LocalizationManager.get("Healthy"), LocalizationManager.get("5 days ago"), Color(0xFF4CAF50))
        )
    }

    val titleText = when (selectedCrop) {
        CropType.WHEAT -> LocalizationManager.get("Wheat Analysis")
        CropType.RICE -> LocalizationManager.get("Rice Analysis")
        CropType.MULTI_CROP -> LocalizationManager.get("Multi-Crop Analysis")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        titleText, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            CropScannerContent(
                autoLaunch = autoLaunch,
                selectedCrop = selectedCrop,
                navController = navController
            )
        }
    }
}

@Composable
fun CropScannerContent(
    autoLaunch: Boolean = false,
    selectedCrop: CropType,
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var ortEnvironment by remember { mutableStateOf<OrtEnvironment?>(null) }
    var ortSession by remember { mutableStateOf<OrtSession?>(null) }
    var analysisResult by remember { mutableStateOf<PredictionResult?>(null) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var modelError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            errorMessage = "Camera permission denied."
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }



    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val bitmap = loadAndScaleBitmap(context, currentPhotoUri!!)
                    if (bitmap == null) {
                        withContext(Dispatchers.Main) { errorMessage = "Failed to load image." }
                        return@launch
                    }
                    withContext(Dispatchers.Main) {
                        capturedImage = bitmap
                    }

                    val session = ortSession
                    val env = ortEnvironment
                    if (session != null && env != null) {
                        withContext(Dispatchers.Main) { isAnalyzing = true }
                        val result = runModelOnBitmap(bitmap, session, env, selectedCrop.classLabels, selectedCrop.unknownLabel, selectedCrop.useImageNetNormalization)
                        withContext(Dispatchers.Main) {
                            result.onSuccess {
                                analysisResult = it
                                errorMessage = null
                            }.onFailure {
                                errorMessage = "Analysis failed: ${it.message}"
                                analysisResult = null
                            }
                            isAnalyzing = false
                        }
                    } else {
                        withContext(Dispatchers.Main) { errorMessage = "Model not loaded yet." }
                    }
                } catch (e: Throwable) {
                    if (com.bharatkrishi.app.BuildConfig.DEBUG) {
                        Log.e("SoilInfoScreen", "Error processing image", e)
                    }
                    withContext(Dispatchers.Main) {
                        errorMessage = "Error processing image: ${e.message}"
                        isAnalyzing = false
                    }
                }
            }
        }
    }
    
    LaunchedEffect(autoLaunch) {
        if (autoLaunch) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val values = android.content.ContentValues().apply {
                    put(MediaStore.Images.Media.TITLE, "New Picture")
                    put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                currentPhotoUri = uri
                if (uri != null) {
                    cameraLauncher.launch(uri)
                }
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val bitmap = loadAndScaleBitmap(context, uri)
                    if (bitmap == null) {
                        withContext(Dispatchers.Main) { errorMessage = "Failed to load image." }
                        return@launch
                    }
                    withContext(Dispatchers.Main) {
                        capturedImage = bitmap
                    }

                    val session = ortSession
                    val env = ortEnvironment
                    if (session != null && env != null) {
                        withContext(Dispatchers.Main) { isAnalyzing = true }
                        val result = runModelOnBitmap(bitmap, session, env, selectedCrop.classLabels, selectedCrop.unknownLabel, selectedCrop.useImageNetNormalization)
                        withContext(Dispatchers.Main) {
                            result.onSuccess {
                                analysisResult = it
                                errorMessage = null
                            }.onFailure {
                                errorMessage = "Analysis failed: ${it.message}"
                                analysisResult = null
                            }
                            isAnalyzing = false
                        }
                    } else {
                        withContext(Dispatchers.Main) { errorMessage = "Model not loaded." }
                    }
                } catch (e: Throwable) {
                    if (com.bharatkrishi.app.BuildConfig.DEBUG) {
                        Log.e("SoilInfoScreen", "Error loading image", e)
                    }
                    withContext(Dispatchers.Main) {
                        errorMessage = "Error loading image: ${e.message}"
                        isAnalyzing = false
                    }
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    // Load/reload model whenever selectedCrop changes
    LaunchedEffect(selectedCrop) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    modelError = null
                    analysisResult = null
                }

                // Close existing session
                try { ortSession?.close() } catch (_: Exception) { }
                withContext(Dispatchers.Main) { ortSession = null }

                val env = ortEnvironment ?: OrtEnvironment.getEnvironment().also { ortEnvironment = it }

                val modelFile = loadOnnxModel(context, selectedCrop.modelFileName)
                val session = env.createSession(modelFile.absolutePath)

                withContext(Dispatchers.Main) {
                    ortSession = session
                }
                Log.d("ONNX", "Model loaded successfully: ${selectedCrop.modelFileName}")
            } catch (e: Exception) {
                Log.e("ONNX", "Error loading ONNX model: ${selectedCrop.modelFileName}", e)
                withContext(Dispatchers.Main) {
                    modelError = "Failed to load model: ${e.message}"
                }
            }
        }
    }

    
    val firebaseManager = remember { FirebaseManager() }
    val gpsManager = remember { GPSLocationManager(context) }

    LaunchedEffect(analysisResult) {
        analysisResult?.let { result ->
            val cropDisplayName = when (selectedCrop) {
                CropType.WHEAT -> LocalizationManager.get("Wheat")
                CropType.RICE -> LocalizationManager.get("Rice")
                CropType.MULTI_CROP -> LocalizationManager.get("Multi-Crop")
            }
            val statusColor = if (result.label == "Healthy") Color(0xFF4CAF50) else Color(0xFFFF9800)
            localRecentScans.add(0, ScanResult(
                cropName = cropDisplayName,
                result = result.label,
                time = LocalizationManager.get("Just now"),
                statusColor = statusColor
            ))

            if (result.label != selectedCrop.unknownLabel && result.label != "Healthy") {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId == null) return@let

                try {
                    val loc = gpsManager.getCurrentLocation()
                    if (loc != null) {
                        firebaseManager.saveDetection(
                            userId = userId,
                            diseaseName = result.label,
                            confidence = result.confidence,
                            imageUrl = "",
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            locationName = "Auto-Detected"
                        )
                    } else {
                        Log.w("Detection", "Skipping saving detection: GPS is unavailable")
                    }
                } catch (e: Exception) {
                    Log.e("Detection", "Failed to save detection to Firebase", e)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        LocalizationManager.get("Scan Your Crop"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        LocalizationManager.get("Take a photo of your crop leaves to detect diseases, pests, and nutrient deficiencies"),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { navController.navigate("crop_selection") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(LocalizationManager.get("Change Crop Type: ${selectedCrop.displayName}"))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (!hasPermission) {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            } else {
                                
                                try {
                                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                                    val imageFileName = "JPEG_" + timeStamp + "_"
                                    val storageDir = java.io.File(context.cacheDir, "my_images")
                                    if (!storageDir.exists()) storageDir.mkdirs()
                                    val imageFile = java.io.File.createTempFile(
                                        imageFileName,  
                                        ".jpg",  
                                        storageDir  
                                    )
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        context.packageName + ".fileprovider",
                                        imageFile
                                    )
                                    currentPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                } catch (e: Exception) {
                                    errorMessage = "Could not create image file: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(LocalizationManager.get("Open Camera & Analyze"))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(LocalizationManager.get("Select from Gallery"))
                    }


    
                    Spacer(modifier = Modifier.height(8.dp))
                    if (ortSession == null && modelError == null) {
                        Text(
                            LocalizationManager.get("Loading AI model..."),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    modelError?.let {
                        Text(
                            it,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    errorMessage?.let {
                        Text(
                            it,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (isAnalyzing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                LocalizationManager.get("Analyzing crop..."),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        
        capturedImage?.let { bitmap ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            LocalizationManager.get("Analyzed Image"),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Captured Crop Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(ratio)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                }
            }
        }

        
        analysisResult?.let { result ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                LocalizationManager.get("AI Diagnosis"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        
                         if (result.confidence > 0.70f && result.label != "Healthy" && !result.label.contains("Healthy") && result.label != selectedCrop.unknownLabel) {
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudUpload, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    LocalizationManager.get("Reported to Disease Surveillance Network"), 
                                    fontSize = 10.sp, 
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        
                        Text(
                            text = LocalizationManager.get(result.label),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${LocalizationManager.get("Confidence")}: ${(result.confidence * 100).toInt()}%",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (result.label == selectedCrop.unknownLabel || result.label == "Unknown") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = LocalizationManager.get("Please take a different photo."),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        
                        if (result.confidence < 0.30f) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Low Confidence",
                                        tint = Color(0xFFFF9800)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            LocalizationManager.get("Low Confidence"),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100),
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            LocalizationManager.get("Please take the photo again. Ensure the leaf is in focus and well-lit."),
                                            fontSize = 12.sp,
                                            color = Color(0xFFEF6C00)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        
                        Text(
                            LocalizationManager.get("Confidence Distribution"),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        result.distribution.take(5).forEach { (label, score) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    LocalizationManager.get(label),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 12.sp
                                )
                                LinearProgressIndicator(
                                    progress = score.coerceIn(0f, 1f),
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${(score * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }

        
        item {
            Text(
                LocalizationManager.get("How it works"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            val steps = listOf(
                ScanStep("1", LocalizationManager.get("Take Photo"), LocalizationManager.get("Capture clear image of affected crop leaves"), Icons.Default.Camera),
                ScanStep("2", LocalizationManager.get("AI Analysis"), LocalizationManager.get("Our AI analyzes the image for diseases and pests"), Icons.Default.AutoAwesome),
                ScanStep("3", LocalizationManager.get("Get Results"), LocalizationManager.get("Receive diagnosis with treatment recommendations"), Icons.Default.Assignment)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.forEach { step ->
                    StepCard(step)
                }
            }
        }

        
        item {
            Text(
                LocalizationManager.get("Recent Scans"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                localRecentScans.forEach { scan ->
                    RecentScanCard(scan)
                }
            }
        }
    }
}


fun loadOnnxModel(context: Context, modelName: String): File {
    val onnxFile = File(context.filesDir, modelName)
    val dataFile = File(context.filesDir, "$modelName.data")

    // Create parent directories if model path has subdirectories
    onnxFile.parentFile?.mkdirs()

    Log.d("ONNX", "Checking ONNX: ${onnxFile.absolutePath} exists=${onnxFile.exists()}")
    Log.d("ONNX", "Checking DATA: ${dataFile.absolutePath} exists=${dataFile.exists()}")

    
    if (!onnxFile.exists()) {
        Log.d("ONNX", "Copying .onnx file from assets")
        context.assets.open(modelName).use { input ->
            FileOutputStream(onnxFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    
    if (!dataFile.exists()) {
        Log.d("ONNX", "Copying .onnx.data file from assets")
        try {
            context.assets.open("$modelName.data").use { input ->
                FileOutputStream(dataFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.e("ONNX", "ERROR COPYING .data FILE → FILE NOT FOUND IN ASSETS")
        }
    }

    Log.d("ONNX", "Final check: onnx.exists=${onnxFile.exists()}, data.exists=${dataFile.exists()}")

    return onnxFile
}

fun loadAndScaleBitmap(context: Context, uri: android.net.Uri): Bitmap? {
    return try {
        val rawBitmap = if (android.os.Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

        val safeBitmap = ensureBitmapMutable(rawBitmap)

        val maxDim = 1024
        val w = safeBitmap.width
        val h = safeBitmap.height
        if (w > maxDim || h > maxDim) {
            val scale = maxDim.toFloat() / maxOf(w, h)
            val newW = (w * scale).toInt()
            val newH = (h * scale).toInt()
            val scaled = Bitmap.createScaledBitmap(safeBitmap, newW, newH, true)
            if (scaled != safeBitmap) safeBitmap.recycle()
            scaled
        } else {
            safeBitmap
        }
    } catch (e: Throwable) {
        Log.e("BitmapLoad", "Failed to load/scale bitmap", e)
        null
    }
}

fun ensureBitmapMutable(src: Bitmap): Bitmap {
    return try {
        val isHardware = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
            src.config == Bitmap.Config.HARDWARE
        if (isHardware || src.config != Bitmap.Config.ARGB_8888 || !src.isMutable) {
            src.copy(Bitmap.Config.ARGB_8888, true) ?: src
        } else {
            src
        }
    } catch (e: Exception) {
        try {
            src.copy(Bitmap.Config.ARGB_8888, true) ?: src
        } catch (_: Exception) {
            src
        }
    }
}

fun runModelOnBitmap(
    bitmap: Bitmap,
    session: OrtSession,
    env: OrtEnvironment,
    classLabels: Array<String>,
    unknownLabel: String,
    useNormalize: Boolean
): Result<PredictionResult> {
    return try {
        val imgSize = 224
        val safeBitmap = ensureBitmapMutable(bitmap)
        val resized = Bitmap.createScaledBitmap(safeBitmap, imgSize, imgSize, true)

        val input = FloatArray(1 * 3 * imgSize * imgSize)

        var idx = 0
        for (y in 0 until imgSize) {
            for (x in 0 until imgSize) {
                val pixel = resized.getPixel(x, y)

                var r = ((pixel shr 16) and 0xFF) / 255f
                var g = ((pixel shr 8) and 0xFF) / 255f
                var b = (pixel and 0xFF) / 255f

                if (useNormalize) {
                    r = (r - 0.485f) / 0.229f
                    g = (g - 0.456f) / 0.224f
                    b = (b - 0.406f) / 0.225f
                }

                input[idx] = r
                input[idx + imgSize * imgSize] = g
                input[idx + 2 * imgSize * imgSize] = b

                idx++
            }
        }

        val shape = longArrayOf(1, 3, imgSize.toLong(), imgSize.toLong())
        val tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(input), shape)

        val inputName = session.inputNames.first()
        val output = session.run(mapOf(inputName to tensor))[0].value

        val rawScores: FloatArray = when (output) {
            is FloatArray -> output
            is Array<*> -> output[0] as FloatArray
            else -> throw Exception("Unknown output format: ${output!!::class.java}")
        }

        val scores = softMax(rawScores)

        val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: 0
        val label = classLabels.getOrNull(maxIndex) ?: unknownLabel
        val conf = scores[maxIndex]

        val distribution = scores.mapIndexed { index, score ->
            (classLabels.getOrNull(index) ?: unknownLabel) to score
        }.sortedByDescending { it.second }

        Result.success(PredictionResult(label, conf, distribution))
    } catch (e: Exception) {
        Log.e("ONNX", "Bitmap inference failed", e)
        Result.failure(e)
    }
}



@Composable
fun StepCard(step: ScanStep) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    step.number,
                    color = MaterialTheme.colorScheme.onPrimary,
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
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    step.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                step.icon,
                contentDescription = step.title,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun RecentScanCard(scan: ScanResult) {
    Card(
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                scan.cropName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}




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

data class PredictionResult(
    val label: String,
    val confidence: Float,
    val distribution: List<Pair<String, Float>>
)

fun softMax(scores: FloatArray): FloatArray {
    if (scores.isEmpty()) return FloatArray(0)
    val max = scores.maxOrNull() ?: 0f
    val expScores = scores.map { kotlin.math.exp((it - max).toDouble()) }
    val sumExp = expScores.sum()
    if (sumExp <= 0.0) {
        return FloatArray(scores.size) { 1f / scores.size }
    }
    return expScores.map { (it / sumExp).toFloat() }.toFloatArray()
}

