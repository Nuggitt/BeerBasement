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
            val assetManager = context.assets
            val fileDescriptor = assetManager.openFd(modelPath)
            val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val modelByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)

            // Initialize the TensorFlow Lite Interpreter
            interpreter = Interpreter(modelByteBuffer)
            Log.d("TensorFlowModel", "Model loaded successfully.")

            // Checking output tensor shapes (for debugging)
            val outputTensorName = interpreter?.getOutputTensor(0)
            val outputTensorStyle = interpreter?.getOutputTensor(1)
            Log.d("TensorFlowModel", "Output tensor name shape: ${outputTensorName?.shape()?.contentToString()}")
            Log.d("TensorFlowModel", "Output tensor style shape: ${outputTensorStyle?.shape()?.contentToString()}")
            Log.d("TensorFlowModel", "Output tensor abv shape: ${interpreter?.getOutputTensor(2)?.shape()?.contentToString()}")
            Log.d("TensorFlowModel", "Output tensor volume shape: ${interpreter?.getOutputTensor(3)?.shape()?.contentToString()}")
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

        for (pixel in intArray) {
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    // Predict beer details from the ByteBuffer input
    fun predictBeerDetails(byteBuffer: ByteBuffer): Map<String, Any> {
        // Updated to handle a single value output per category (or adjust if the model outputs more than one value per category)
        val outputName = Array(1) { FloatArray(6) }  // Adjust based on your model's output dimensions
        val outputStyle = Array(1) { FloatArray(1) }
        val outputABV = Array(1) { FloatArray(6) }
        val outputVolume = Array(1) { FloatArray(1) }

        interpreter?.runForMultipleInputsOutputs(
            arrayOf(byteBuffer),
            mapOf(0 to outputName, 1 to outputStyle, 2 to outputABV, 3 to outputVolume)
        )

        // Get human-readable results
        val predictedName = mapName(outputName[0])
        val predictedStyle = mapStyle(outputStyle[0])
        val predictedABV = mapABV(outputABV[0])  // Convert ABV to string
        val predictedVolume = mapVolume(outputVolume[0])  // Convert volume to string

        return mapOf(
            "name" to predictedName,
            "style" to predictedStyle,
            "abv" to predictedABV,
            "volume" to predictedVolume
        )
    }

    // Example of how to map model output to human-readable values
    private fun mapStyle(output: FloatArray): String {
        val styleIndex = output[0].toInt()  // The style output is a scalar index
        return when (styleIndex) {
            0 -> "Dark Lager"
            1 -> "Pilsner"
            2 -> "Lager"
            3 -> "Stout"
            4 -> "Pale Lager"
            5 -> "Light Lager"
            else -> "Unknown"
        }
    }


    private fun mapABV(output: FloatArray): String {
        // Assuming the model outputs values between 0 and 1, scale to percentage
        val abv = output[0] * 100  // Scale from [0,1] to [0,100] for percentage
        // Clamp the value to avoid unreasonably high values
        val clampedABV = abv.coerceIn(0f, 100f)
        return "${clampedABV}%"
    }

    private fun mapVolume(output: FloatArray): String {
        val volume = output[0] * 1000 // Scale the predicted volume to milliliters, assuming it’s a fraction of 1000 ml
        return "$volume ml"
    }



    private fun mapName(output: FloatArray): String {
        val index = output.indices.maxByOrNull { output[it] } ?: -1
        return when (index) {
            0 -> "Budweiser"
            1 -> "Carlsberg"
            2 -> "Corona Extra"
            3 -> "Guinness Foreign Extra Stout"
            4 -> "Heineken"
            5 -> "Tuborg Classic"
            else -> "Unknown"
        }
    }
}


