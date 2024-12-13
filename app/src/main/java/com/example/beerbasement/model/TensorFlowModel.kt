package com.example.beerbasement.model

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer

class TensorFlowModel {

    private var tflite: Interpreter? = null

    // Initialize the model
    fun loadModel(context: Context) {
        tflite = loadModelFile(context)
    }

    // Load the TensorFlow Lite model from the assets folder
    private fun loadModelFile(context: Context): Interpreter {
        val modelPath = "beer_model.tflite"  // Make sure the model is in the assets folder
        val assetManager = context.assets
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val modelByteBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        return Interpreter(modelByteBuffer)
    }

    // Run inference with input data (ByteBuffer) and return the predicted beer style
    fun predictBeerStyle(inputData: ByteBuffer): String {
        val output = Array(1) { FloatArray(4) } // Assuming 4 classes in the model output (e.g., Pilsner, IPA, Lager, Stout)

        tflite?.run(inputData, output)

        // Find the class with the highest probability (argmax)
        val predictedClass = output[0].indices.maxByOrNull { output[0][it] } ?: -1

        // Define the beer styles in the order the model was trained
        val beerStyles = arrayOf("Pilsner", "IPA", "Lager", "Stout")

        return if (predictedClass != -1) beerStyles[predictedClass] else "Unknown"
    }

    // Close the interpreter to free resources
    fun close() {
        tflite?.close()
    }
}
