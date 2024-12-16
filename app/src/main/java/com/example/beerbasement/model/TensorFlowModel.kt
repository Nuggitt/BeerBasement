package com.example.beerbasement.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.io.FileInputStream
import java.io.IOException

class TensorFlowModel(private val modelPath: String = "beer_model.tflite") {
    private var interpreter: Interpreter? = null

    // Load the model from assets into the interpreter
    fun loadModel(context: Context) {
        try {
            // Load the model file from assets
            val assetManager = context.assets
            val fileDescriptor = assetManager.openFd(modelPath)
            val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val modelByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)

            // Initialize the TensorFlow Lite Interpreter
            interpreter = Interpreter(modelByteBuffer)
            Log.d("TensorFlowModel", "Model loaded successfully.")
        } catch (e: IOException) {
            Log.e("TensorFlowModel", "Error loading model: ${e.message}")
        }
    }

    // Close the TensorFlow Lite interpreter when no longer needed
    fun close() {
        try {
            interpreter?.close()
            Log.d("TensorFlowModel", "Model closed successfully.")
        } catch (e: Exception) {
            Log.e("TensorFlowModel", "Error closing model: ${e.message}")
        }
    }

    // Convert the Bitmap to ByteBuffer for feeding into the model
    fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputSize = 224 // Resize to 224x224 for the model
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intArray = IntArray(inputSize * inputSize)
        resizedBitmap.getPixels(intArray, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        // Process each pixel and add normalized RGB values to the ByteBuffer
        for (pixel in intArray) {
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            byteBuffer.putFloat(r)  // Red channel
            byteBuffer.putFloat(g)  // Green channel
            byteBuffer.putFloat(b)  // Blue channel
        }

        // Rewind the buffer to the beginning for further processing
        byteBuffer.rewind()

        return byteBuffer
    }

    // Predict beer details from the ByteBuffer input (e.g., beer name, style, etc.)
    fun predictBeerDetails(byteBuffer: ByteBuffer): Map<String, String> {
        val output = Array(1) { FloatArray(6) }  // Ensure this is the correct output size for 6 classes

        // Run the model with the byteBuffer input
        interpreter?.run(byteBuffer, output)

        // Log the raw output for debugging
        Log.d("TensorFlowModel", "Raw output: ${output[0].joinToString(", ")}")

        return mapOf(
            "name" to mapName(output[0]),
            "style" to mapStyle(output[0]),
            "abv" to mapABV(output[0].getOrNull(2) ?: -1.0f),  // Check index before accessing
            "volume" to mapVolume(output[0].getOrNull(3) ?: -1.0f)  // Check index before accessing
        )
    }


    // Example of how to map model output to human-readable values
    private fun mapStyle(output: FloatArray): String {
        val index = output.indices.maxByOrNull { output[it] } ?: -1
        return when (index) {
            0 -> "Lager"
            1 -> "Pilsner"
            2 -> "Stout"
            3 -> "IPA"
            4 -> "Ale"
            else -> "Unknown"
        }
    }

    private fun mapABV(output: Float): String {
        return "${output.toInt()}%"  // Assuming ABV is at index 3
    }

    private fun mapVolume(output: Float): String {
        return "${output.toInt()} ml"  // Assuming volume is at index 4
    }

    private fun mapName(output: FloatArray): String {
        val index = output.indices.maxByOrNull { output[it] } ?: -1

        // Log the raw output to see the model's prediction confidence
        Log.d("TensorFlowModel", "Raw output: ${output.joinToString(", ")}")

        // Set a confidence threshold to avoid incorrect classifications
        val threshold = 0.5f  // Example threshold for confidence

        if (output[index] >= threshold) {
            return when (index) {
                0 -> "Budweiser"
                1 -> "Carlsberg Pilsner"
                2 -> "Corona"
                3 -> "Guinness"
                4 -> "Heineken"
                5 -> "Tuborg Classic"
                else -> "Unknown"
            }
        } else {
            return "Unknown" // If the model isn't confident enough, return "Unknown"
        }
    }



}
