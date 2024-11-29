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
    navController: NavController
) {
    var title by rememberSaveable { mutableStateOf("") }
    var brewery by rememberSaveable { mutableStateOf("") }
    var style by rememberSaveable { mutableStateOf("") }
    var abv by rememberSaveable { mutableStateOf("") }
    var volume by rememberSaveable { mutableStateOf("") }
    var pictureUrl by rememberSaveable { mutableStateOf("") }
    var howMany by rememberSaveable { mutableStateOf("") }
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser?.email ?: "Unknown"
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }


    // Inside your composable
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri?.let { uri ->
                    // Log the URI for debugging
                    Log.d("BeerAdd", "Captured image URI: $uri")
                    // Navigating to the next screen with the captured image URI
                    navController.navigate("imageData/${uri.toString()}")
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
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(start = 16.dp, end = 16.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Beer Name") })
            OutlinedTextField(value = brewery, onValueChange = { brewery = it }, label = { Text("Brewery") })
            OutlinedTextField(value = style, onValueChange = { style = it }, label = { Text("Style") })
            OutlinedTextField(value = abv, onValueChange = { abv = it }, label = { Text("ABV") })
            OutlinedTextField(value = volume, onValueChange = { volume = it }, label = { Text("Volume") })
            OutlinedTextField(value = pictureUrl, onValueChange = { pictureUrl = it }, label = { Text("Picture URL") })
            OutlinedTextField(value = howMany, onValueChange = { howMany = it }, label = { Text("How Many") })

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onNavigateBack, modifier = Modifier.weight(1f)) {
                    Text("Back")
                }
                Button(onClick = {
                    addBeer(Beer(
                        user = currentUser,
                        brewery = brewery,
                        name = title,
                        style = style,
                        abv = abv.toFloatOrNull() ?: 0f,
                        volume = volume.toFloatOrNull() ?: 0f,
                        pictureUrl = pictureUrl,
                        howMany = howMany.toIntOrNull() ?: 0
                    ))
                    onNavigateBack()
                }, modifier = Modifier.weight(1f)) {
                    Text("Add Beer")
                }

                Button(onClick = {
                    val uri = createImageFile(context)
                    if (uri != null) {
                        imageUri = uri
                        takePictureLauncher.launch(uri)
                    } else {
                        Toast.makeText(context, "Failed to create image file.", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Take Picture")
                }
            }
        }
    }
}



