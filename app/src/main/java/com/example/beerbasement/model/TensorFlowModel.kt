package com.example.beerbasement.model

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder

import android.util.Log

class TensorFlowModel {

    private var tflite: Interpreter? = null
    private val TAG = "TensorFlowModel"

    fun loadModel(context: Context) {
        try {
            tflite = loadModelFile(context)
            Log.d(TAG, "Model loaded successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model: ${e.message}")
        }
    }

    private fun loadModelFile(context: Context): Interpreter {
        val modelPath = "beer_model.tflite"
        val assetManager = context.assets
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val modelByteBuffer: MappedByteBuffer =
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        return Interpreter(modelByteBuffer)
    }

    fun predictBeerStyle(byteBuffer: ByteBuffer): String {
        return try {
            val output = Array(1) { FloatArray(4) } // Adjust size based on model's output
            tflite?.run(byteBuffer, output)

            val predictedClass = output[0].indices.maxByOrNull { output[0][it] } ?: -1
            val beerStyles = arrayOf("Pilsner", "IPA", "Lager", "Stout")

            if (predictedClass != -1) beerStyles[predictedClass] else "Unknown"
        } catch (e: Exception) {
            Log.e(TAG, "Error during prediction: ${e.message}")
            "Error predicting beer style"
        }
    }

    fun predictBeerDetails(byteBuffer: ByteBuffer): Map<String, String> {
        return mapOf(
            "name" to "Tuborg Classic",
            "style" to "Lager",
            "abv" to "5.0%",
            "volume" to "330ml"
        )
    }

    fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputSize = 224
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intArray = IntArray(inputSize * inputSize)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in intArray) {
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        return byteBuffer
    }

    fun close() {
        try {
            tflite?.close()
            Log.d(TAG, "Model closed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing model: ${e.message}")
        }
    }
}

