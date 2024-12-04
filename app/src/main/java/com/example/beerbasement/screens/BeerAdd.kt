package com.example.beerbasement.screens

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.beerbasement.NavRoutes
import com.example.beerbasement.model.Beer
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun createImageFile(context: Context): Uri? {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val file = File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

private const val CAMERA_PERMISSION_REQUEST_CODE = 100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerAdd(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    addBeer: (Beer) -> Unit = {},
    signOut: () -> Unit = { FirebaseAuth.getInstance().signOut() },
    navController: NavController,
    imageUri: String, // Updated to String
    recognizedLogos: String,
    recognizedText: String,
    recognizedLabels: String
) {
    var title by rememberSaveable { mutableStateOf("") }
    var brewery by rememberSaveable { mutableStateOf("") }
    var style by rememberSaveable { mutableStateOf("") }
    var abv by rememberSaveable { mutableStateOf("") }
    var volume by rememberSaveable { mutableStateOf("") }
    var pictureUrl by rememberSaveable { mutableStateOf(imageUri) } // Pass picture URI here
    var howMany by rememberSaveable { mutableStateOf("") }
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser?.email ?: "Unknown"
    val context = LocalContext.current
    var imageUriLocal by remember { mutableStateOf<Uri?>(null) }

    // Autofill fields based on recognized data
    LaunchedEffect(recognizedLogos, recognizedText, recognizedLabels) {
        if (recognizedLogos.isNotEmpty()) {
            brewery = recognizedLogos // Assuming the recognized logos can provide the brewery name
        }
        if (recognizedText.isNotEmpty()) {
            title = recognizedText // Assuming recognized text contains the beer title
        }
        if (recognizedLabels.isNotEmpty()) {
            // You may want to match certain labels to style and ABV
            // Here we simply set the first label to style and ABV for simplicity
            val labels = recognizedLabels.split(",")
            style = labels.getOrNull(0) ?: ""
            abv = labels.getOrNull(1) ?: ""
        }
    }

    // Take Picture Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUriLocal?.let { uri ->
                    // Optionally navigate to a new screen
                    val encodedUri = Uri.encode(uri.toString())
                    navController.navigate(NavRoutes.ImageDataScreen.createRoute(encodedUri))
                } ?: run {
                    Log.e("BeerAdd", "Image URI is null")
                }
            } else {
                Log.e("BeerAdd", "Image capture failed or no image selected")
            }
        }
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                title = { Text("Add Beer") },
                actions = {
                    IconButton(onClick = { signOut() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .padding(innerPadding)
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxSize(), // Ensures the LazyColumn takes up full available space
            contentPadding = PaddingValues(bottom = 16.dp) // Adds some bottom padding for the content
        ) {
            // Beer Name
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Beer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Brewery
            item {
                OutlinedTextField(
                    value = brewery,
                    onValueChange = { brewery = it },
                    label = { Text("Brewery") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Style
            item {
                OutlinedTextField(
                    value = style,
                    onValueChange = { style = it },
                    label = { Text("Style") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // ABV
            item {
                OutlinedTextField(
                    value = abv,
                    onValueChange = { abv = it },
                    label = { Text("ABV") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Volume
            item {
                OutlinedTextField(
                    value = volume,
                    onValueChange = { volume = it },
                    label = { Text("Volume") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Picture URL (for now, you can leave it empty or set it later)
            item {
                OutlinedTextField(
                    value = pictureUrl,
                    onValueChange = { pictureUrl = it },
                    label = { Text("Picture URL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // How Many
            item {
                OutlinedTextField(
                    value = howMany,
                    onValueChange = { howMany = it },
                    label = { Text("How Many") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Buttons Row
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Back Button
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back")
                    }
                    // Add Beer Button
                    Button(
                        onClick = {
                            // Add beer logic
                            addBeer(
                                Beer(
                                    user = currentUser,
                                    brewery = brewery,
                                    name = title,
                                    style = style,
                                    abv = abv.toFloatOrNull() ?: 0f,
                                    volume = volume.toFloatOrNull() ?: 0f,
                                    pictureUrl = pictureUrl,
                                    howMany = howMany.toIntOrNull() ?: 0
                                )
                            )
                            // Navigate to Beer List Screen after adding the beer
                            navController.navigate(NavRoutes.BeerList.route)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Beer")
                    }
                    // Take Picture Button
                    Button(
                        onClick = {
                            val hasCameraPermission = ContextCompat.checkSelfPermission(
                                context, android.Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (!hasCameraPermission) {
                                ActivityCompat.requestPermissions(
                                    context as Activity,
                                    arrayOf(android.Manifest.permission.CAMERA),
                                    CAMERA_PERMISSION_REQUEST_CODE
                                )
                            } else {
                                // Proceed to take a picture
                                val uri = createImageFile(context)
                                if (uri != null) {
                                    imageUriLocal = uri
                                    takePictureLauncher.launch(uri)
                                } else {
                                    Toast.makeText(context, "Failed to create image file.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Take Picture")
                    }
                }
            }
        }
    }
}





