package com.example.beerbasement.model

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TensorFlowModel {

    private var tflite: Interpreter? = null

    // Initialize the model
    fun loadModel(context: Context) {
        tflite = loadModelFile(context)
    }

    // Load the TensorFlow Lite model from the assets folder
    private fun loadModelFile(context: Context): Interpreter {
        try {
            val modelPath = "beer_model.tflite"
            val assetManager = context.assets
            val fileDescriptor = assetManager.openFd(modelPath)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val modelByteBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            return Interpreter(modelByteBuffer)
        } catch (e: Exception) {
            // Handle the exception (e.g., log or rethrow)
            throw RuntimeException("Error loading TensorFlow Lite model", e)
        }
    }

    // Run inference with Bitmap and return the predicted beer style
    fun predictBeerStyle(bitmap: Bitmap): String {
        // Convert the bitmap into a ByteBuffer
        val byteBuffer = convertBitmapToByteBuffer(bitmap)

        // Run inference
        val output = Array(1) { FloatArray(4) } // Assuming 4 classes in the model output (e.g., Pilsner, IPA, Lager, Stout)
        tflite?.run(byteBuffer, output)

        // Find the class with the highest probability (argmax)
        val predictedClass = output[0].indices.maxByOrNull { output[0][it] } ?: -1

        // Define the beer styles in the order the model was trained
        val beerStyles = arrayOf("Pilsner", "IPA", "Lager", "Stout")

        return if (predictedClass != -1) beerStyles[predictedClass] else "Unknown"
    }

    // Convert Bitmap to ByteBuffer
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputSize = 224 // Model input size
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3) // 3 channels (RGB)
        byteBuffer.order(ByteOrder.nativeOrder())

        // Resize and convert the bitmap to a ByteBuffer
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

    // Close the interpreter to free resources
    fun close() {
        tflite?.close()
    }
}
