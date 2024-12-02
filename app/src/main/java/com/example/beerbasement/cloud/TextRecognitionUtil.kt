package com.example.beerbasement.cloud

import com.google.android.gms.vision.text.TextRecognizer
import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import android.util.Log

object TextRecognitionUtil {

    fun detectTextInImage(context: Context, bitmap: Bitmap): String {
        // Create a TextRecognizer instance
        val textRecognizer = TextRecognizer.Builder(context).build()

        if (!textRecognizer.isOperational) {
            Log.e("TextRecognition", "Text recognizer is not operational")
            return "Error with Text Recognition"
        }

        // Convert Bitmap to Frame
        val frame = Frame.Builder().setBitmap(bitmap).build()

        // Detect text in the frame
        val items = textRecognizer.detect(frame)

        val detectedText = StringBuilder()
        for (index in 0 until items.size()) {
            val textBlock: TextBlock = items.valueAt(index)
            detectedText.append(textBlock.value).append("\n")
        }

        // Return the recognized text or indicate if no text was found
        return if (detectedText.isNotEmpty()) detectedText.toString() else "No text detected"
    }
}
