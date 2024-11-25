package com.example.beerbasement.screens

import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File

@Composable
fun CameraScreen(onImageCaptured: (Uri) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = ContextCompat.getMainExecutor(context)

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    // Camera preview setup
    Box(modifier = Modifier.fillMaxSize()) {
        val previewView = remember { PreviewView(context) }
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        ) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (exc: Exception) {
                    Log.e("CameraScreen", "Use case binding failed", exc)
                }
            }, executor)
        }

        // Capture button
        Button(
            onClick = {
                imageCapture?.let { capture ->
                    val photoFile = File(
                        context.externalMediaDirs.first(),
                        "captured_image_${System.currentTimeMillis()}.jpg"
                    )
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    capture.takePicture(
                        outputOptions,
                        executor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val savedUri = Uri.fromFile(photoFile)
                                Log.d("CameraScreen", "Image captured: $savedUri")
                                onImageCaptured(savedUri)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraScreen", "Image capture failed: ${exception.message}")
                            }
                        }
                    )
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text("Capture")
        }
    }
}

