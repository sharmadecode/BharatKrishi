package com.bharatkrishi.app.models

import android.content.Context
import android.content.SharedPreferences

/**
 * Defines the available crop detection models and their configurations.
 * Each CropType maps to an ONNX model file, its class labels, and display metadata.
 */
enum class CropType(
    val modelFileName: String,
    val displayName: String,
    val description: String,
    val classLabels: Array<String>,
    val unknownLabel: String,
    val emoji: String,
    val useImageNetNormalization: Boolean
) {
    WHEAT(
        modelFileName = "onnxwheattrb/mobilevit_trb.onnx",
        displayName = "Wheat",
        description = "Wheat disease detection (9 classes)",
        classLabels = arrayOf(
            "Brown Rust",
            "Fusarium Foot Rot",
            "Healthy",
            "Powdery Mildew",
            "Septoria",
            "Smut",
            "Unknown",
            "Wheat Blast",
            "Yellow Rust"
        ),
        unknownLabel = "This is not a wheat crop",
        emoji = "🌾",
        useImageNetNormalization = false
    ),

    RICE(
        modelFileName = "onnxrice/mobilevit_rice_disease.onnx",
        displayName = "Rice",
        description = "Rice disease detection (7 classes)",
        classLabels = arrayOf(
            "Bacterial Leaf Blight",
            "Brown Spot",
            "Healthy",
            "Leaf Blast",
            "Leaf Scald",
            "Leaf Smut",
            "Tungro"
        ),
        unknownLabel = "This is not a rice crop",
        emoji = "🌾",
        useImageNetNormalization = false
    ),

    MULTI_CROP(
        modelFileName = "multicroponnx/mobilevit_multicrop.onnx",
        displayName = "Multi-Crop",
        description = "Potato, Soyabean, Chilli, Pumpkin (23 classes)",
        classLabels = arrayOf(
            "Chili Bacterial Spot", "Chili Cercospora", "Chili Curl Virus", "Chili Healthy",
            "Chili Nutrition Deficiency", "Chili White Spot", "Potato Bacteria", "Potato Fungi",
            "Potato Healthy", "Potato Pest", "Potato Phytopthora", "Potato Virus",
            "Pumpkin Bacterial Leaf Spot", "Pumpkin Downy Mildew", "Pumpkin Healthy",
            "Pumpkin Mosaic", "Pumpkin Powdery Mildew", "Soybean Frog Eye", "Soybean Healthy",
            "Soybean Mosaic", "Soybean Pest", "Soybean Rust", "Soybean Septoria"
        ),
        unknownLabel = "Unrecognized crop",
        emoji = "🌿",
        useImageNetNormalization = true
    );

    companion object {
        fun fromName(name: String): CropType {
            return entries.find { it.name == name } ?: WHEAT
        }
    }
}

/**
 * Manages the user's crop model preference using SharedPreferences.
 */
class CropPreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("crop_preferences", Context.MODE_PRIVATE)

    fun getSelectedCrop(): CropType {
        val name = prefs.getString("selected_crop", CropType.WHEAT.name) ?: CropType.WHEAT.name
        return CropType.fromName(name)
    }

    fun setSelectedCrop(crop: CropType) {
        prefs.edit().putString("selected_crop", crop.name).apply()
    }
}
