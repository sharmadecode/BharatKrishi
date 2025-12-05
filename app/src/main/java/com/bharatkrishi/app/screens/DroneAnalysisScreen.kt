package com.bharatkrishi.app.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.FloatBuffer
import kotlin.math.roundToInt

// ---------- LABELS & CONSTANTS ----------
private val DRONE_CLASS_LABELS = arrayOf(
    "Healthy", "Yellow rust", "Brown rust", "Loose Smut",
    "Unknown", "Septoria", "Stripe rust", "Mildew"
)

private const val PATCH_SIZE = 256

enum class SeverityGroup { HEALTHY, DISEASED, UNKNOWN }

data class PatchResult(
    val row: Int,
    val col: Int,
    val classIndex: Int,
    val label: String,
    val confidence: Float,
    val severity: SeverityGroup
)

data class DroneAnalysisSummary(
    val totalPatches: Int,
    val healthyCount: Int,
    val diseasedCount: Int,
    val unknownCount: Int,
    val classHistogram: Map<String, Int>
) {
    val healthyPercent: Float get() = if (totalPatches == 0) 0f else healthyCount * 100f / totalPatches
    val diseasedPercent: Float get() = if (totalPatches == 0) 0f else diseasedCount * 100f / totalPatches
    val unknownPercent: Float get() = if (totalPatches == 0) 0f else unknownCount * 100f / totalPatches
}

enum class DroneLayer { DISEASE_HEATMAP, NDVI_LAYER_PLACEHOLDER }

// ---------- MAIN SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DroneAnalysisScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var ortEnvironment by remember { mutableStateOf<OrtEnvironment?>(null) }
    var ortSession by remember { mutableStateOf<OrtSession?>(null) }
    var modelError by remember { mutableStateOf<String?>(null) }

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var patchResults by remember { mutableStateOf<List<PatchResult>>(emptyList()) }
    var summary by remember { mutableStateOf<DroneAnalysisSummary?>(null) }
    var selectedLayer by remember { mutableStateOf(DroneLayer.DISEASE_HEATMAP) }

    // LOAD ONNX MODEL -----------------
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val env = OrtEnvironment.getEnvironment()
                ortEnvironment = env
                val modelFile = loadOnnxModel(context, "mobilevit_wheat_8class.onnx")
                ortSession = env.createSession(modelFile.absolutePath)
            } catch (e: Exception) {
                modelError = "Failed to load model: ${e.message}"
            }
        }
    }

    // GALLERY PICKER -------------------
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bmp = loadBitmapFromUri(context, uri)
            if (bmp == null) {
                modelError = "Failed to decode image."
                return@rememberLauncherForActivityResult
            }

            selectedBitmap = bmp
            summary = null
            patchResults = emptyList()

            val session = ortSession
            val env = ortEnvironment

            if (session == null || env == null) {
                modelError = "AI model not ready yet."
                return@rememberLauncherForActivityResult
            }

            isProcessing = true

            scope.launch(Dispatchers.Default) {
                try {
                    val results = analyzeDroneImage(bmp, session, env)
                    val stats = buildSummary(results)
                    patchResults = results
                    summary = stats
                } catch (e: Exception) {
                    modelError = "Error during analysis: ${e.message}"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    // UI -------------------------
    val scroll = rememberScrollState()
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drone Field Analysis", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surface)
            )
        },
        containerColor = bg
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---------- HERO CARD ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Analyze Entire Fields", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Upload a drone image to generate a full-field disease map.",
                        color = Color.Gray, fontSize = 14.sp)
                }
            }

            // ---------- UPLOAD CARD ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Upload Drone Image", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Select Image")
                    }

                    modelError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = Color.Red, fontSize = 12.sp)
                    }

                    selectedBitmap?.let { bmp ->
                        Spacer(Modifier.height(10.dp))
                        Text("Preview:", fontWeight = FontWeight.SemiBold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(bmp.asImageBitmap(), contentDescription = null)
                        }
                    }

                    if (isProcessing) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Analyzing field…", fontSize = 12.sp)
                        }
                    }
                }
            }

            // ---------- SHOW RESULTS IF AVAILABLE ----------
            if (summary != null && patchResults.isNotEmpty()) {

                val s = summary!!

                // LAYER SWITCHER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Layers", fontWeight = FontWeight.Bold)

                    Row {
                        FilterChip(
                            selected = selectedLayer == DroneLayer.DISEASE_HEATMAP,
                            onClick = { selectedLayer = DroneLayer.DISEASE_HEATMAP },
                            label = { Text("Disease Heatmap") }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = selectedLayer == DroneLayer.NDVI_LAYER_PLACEHOLDER,
                            onClick = { selectedLayer = DroneLayer.NDVI_LAYER_PLACEHOLDER },
                            label = { Text("NDVI (Future)") }
                        )
                    }
                }
                // ---------- SUMMARY CARDS ----------
                // Summary cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DroneMetricCard(
                        title = "Field Health",
                        primaryText = "${s.healthyPercent.roundToInt()}% Healthy",
                        secondaryText = "${s.diseasedPercent.roundToInt()}% Diseased",
                        accentColor = Color(0xFF2E7D32),
                        icon = Icons.Default.Poll,
                        modifier = Modifier.weight(1f)   // ✔ weight here
                    )

                    DroneMetricCard(
                        title = "Patches Analyzed",
                        primaryText = "${s.totalPatches}",
                        secondaryText = "${s.classHistogram.keys.size} classes detected",
                        accentColor = MaterialTheme.colorScheme.primary,
                        icon = Icons.Default.GridView,
                        modifier = Modifier.weight(1f)   // ✔ weight here
                    )
                }

                // ---------- CLASS HISTOGRAM ----------
                DroneClassBreakdownCard(s)

                // ---------- SELECTED LAYER ----------
                when (selectedLayer) {
                    DroneLayer.DISEASE_HEATMAP -> DroneHeatmapCard(patchResults)
                    DroneLayer.NDVI_LAYER_PLACEHOLDER -> NdviPlaceholderCard()
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////////////////
// METRIC CARD
///////////////////////////////////////////////////////////////////////
@Composable
private fun DroneMetricCard(
    title: String,
    primaryText: String,
    secondaryText: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier    // <-- IMPORTANT
) {
    Card(
        modifier = modifier,          // <-- NO weight() inside
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                primaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                secondaryText,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

///////////////////////////////////////////////////////////////////////
// CLASS HISTOGRAM CARD
///////////////////////////////////////////////////////////////////////
@Composable
private fun DroneClassBreakdownCard(summary: DroneAnalysisSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Poll, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("8-Class Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            summary.classHistogram.entries.sortedByDescending { it.value }.forEach { (label, count) ->
                val percent = if (summary.totalPatches == 0) 0f else count * 100f / summary.totalPatches

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp)

                    LinearProgressIndicator(
                        progress = percent / 100f,         // ✔ FIXED (no lambda)
                        modifier = Modifier
                            .weight(2f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(Modifier.width(8.dp))
                    Text("${percent.roundToInt()}%", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

///////////////////////////////////////////////////////////////////////
// HEATMAP CARD (Canvas)
///////////////////////////////////////////////////////////////////////
@Composable
private fun DroneHeatmapCard(patchResults: List<PatchResult>) {

    if (patchResults.isEmpty()) return

    val rows = (patchResults.maxOfOrNull { it.row } ?: 0) + 1
    val cols = (patchResults.maxOfOrNull { it.col } ?: 0) + 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Disease Heatmap", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(cols.toFloat() / rows.toFloat())
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.05f))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {

                    val cellW = size.width / cols
                    val cellH = size.height / rows

                    patchResults.forEach { patch ->

                        val color = when (patch.severity) {
                            SeverityGroup.HEALTHY -> Color(0xFF2E7D32).copy(alpha = 0.85f)
                            SeverityGroup.DISEASED -> Color(0xFFD32F2F).copy(alpha = 0.85f)
                            SeverityGroup.UNKNOWN -> Color(0xFF9E9E9E).copy(alpha = 0.85f)
                        }

                        drawRect(
                            color = color,
                            topLeft = androidx.compose.ui.geometry.Offset(
                                x = patch.col * cellW,
                                y = patch.row * cellH
                            ),
                            size = androidx.compose.ui.geometry.Size(cellW, cellH)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HeatmapLegendDot(Color(0xFF2E7D32), "Healthy")
                HeatmapLegendDot(Color(0xFFD32F2F), "Diseased")
                HeatmapLegendDot(Color(0xFF9E9E9E), "Unknown")
            }
        }
    }
}

///////////////////////////////////////////////////////////////////////
// LEGEND DOT
///////////////////////////////////////////////////////////////////////
@Composable
private fun HeatmapLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 11.sp)
    }
}

///////////////////////////////////////////////////////////////////////
// NDVI PLACEHOLDER
///////////////////////////////////////////////////////////////////////
@Composable
private fun NdviPlaceholderCard() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFA000))
                Spacer(Modifier.width(8.dp))
                Text("NDVI Layer (Coming Soon)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Text(
                "This placeholder will show NDVI maps from your drone or backend in future updates.",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

///////////////////////////////////////////////////////////////////////
// SEVERITY GROUP MAPPING
///////////////////////////////////////////////////////////////////////
private fun mapIndexToSeverity(idx: Int): SeverityGroup {
    return when (idx) {
        0 -> SeverityGroup.HEALTHY
        4 -> SeverityGroup.UNKNOWN
        else -> SeverityGroup.DISEASED
    }
}

///////////////////////////////////////////////////////////////////////
// SUMMARY BUILDER
///////////////////////////////////////////////////////////////////////
private fun buildSummary(results: List<PatchResult>): DroneAnalysisSummary {

    val total = results.size
    var healthy = 0
    var diseased = 0
    var unknown = 0

    val histogram = mutableMapOf<String, Int>()

    results.forEach { r ->
        when (r.severity) {
            SeverityGroup.HEALTHY -> healthy++
            SeverityGroup.DISEASED -> diseased++
            SeverityGroup.UNKNOWN -> unknown++
        }
        histogram[r.label] = (histogram[r.label] ?: 0) + 1
    }

    return DroneAnalysisSummary(total, healthy, diseased, unknown, histogram)
}

///////////////////////////////////////////////////////////////////////
// ONNX PATCH INFERENCE
///////////////////////////////////////////////////////////////////////
private fun analyzeDroneImage(bitmap: Bitmap, session: OrtSession, env: OrtEnvironment): List<PatchResult> {

    val rows = bitmap.height / PATCH_SIZE
    val cols = bitmap.width / PATCH_SIZE

    val results = mutableListOf<PatchResult>()

    for (r in 0 until rows) {
        for (c in 0 until cols) {

            val patch = Bitmap.createBitmap(bitmap, c * PATCH_SIZE, r * PATCH_SIZE, PATCH_SIZE, PATCH_SIZE)
            val (cls, conf) = runModelOnPatch(patch, session, env)

            results.add(
                PatchResult(
                    row = r,
                    col = c,
                    classIndex = cls,
                    label = DRONE_CLASS_LABELS.getOrNull(cls) ?: "Unknown",
                    confidence = conf,
                    severity = mapIndexToSeverity(cls)
                )
            )
        }
    }

    return results
}

private fun runModelOnPatch(bitmap: Bitmap, session: OrtSession, env: OrtEnvironment): Pair<Int, Float> {

    val resized = Bitmap.createScaledBitmap(bitmap, PATCH_SIZE, PATCH_SIZE, true)

    val input = FloatArray(3 * PATCH_SIZE * PATCH_SIZE)
    var idx = 0

    for (y in 0 until PATCH_SIZE) {
        for (x in 0 until PATCH_SIZE) {

            val pixel = resized.getPixel(x, y)
            input[idx] = ((pixel shr 16) and 0xFF) / 255f
            input[idx + PATCH_SIZE * PATCH_SIZE] = ((pixel shr 8) and 0xFF) / 255f
            input[idx + 2 * PATCH_SIZE * PATCH_SIZE] = (pixel and 0xFF) / 255f

            idx++
        }
    }

    val tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(input), longArrayOf(1, 3, PATCH_SIZE.toLong(), PATCH_SIZE.toLong()))
    val output = session.run(mapOf(session.inputNames.first() to tensor))[0].value

    val scores: FloatArray = when (output) {
        is FloatArray -> output
        is Array<*> -> output[0] as FloatArray
        else -> throw Exception("Unexpected ONNX output type")
    }

    var bestIndex = 0
    var bestScore = scores[0]

    for (i in 1 until scores.size) {
        if (scores[i] > bestScore) {
            bestScore = scores[i]
            bestIndex = i
        }
    }

    return bestIndex to bestScore
}

///////////////////////////////////////////////////////////////////////
// BITMAP LOADER (SAFE)
///////////////////////////////////////////////////////////////////////
private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        val originalBitmap =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

        // ⭐ FORCE convert HARDWARE → ARGB_8888 (mutable)
        originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

    } catch (e: Exception) {
        Log.e("DRONE_BITMAP", "Error decoding bitmap from uri", e)
        null
    }
}
