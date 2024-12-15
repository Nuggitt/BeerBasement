package com.example.beerbasement.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.beerbasement.NavRoutes
import com.example.beerbasement.model.Beer
import com.google.firebase.auth.FirebaseAuth

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
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser?.email ?: "Unknown"
    val context = LocalContext.current

    // Request camera permissions
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as ComponentActivity,
                arrayOf(Manifest.permission.CAMERA),
                1
            )
        }
    }

    // Camera intent launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap?
            imageBitmap?.let {
                val imageUriString = MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    it,
                    "Beer Image",
                    ""
                )
                imageUri = Uri.parse(imageUriString)
            }
        } else {
            Toast.makeText(context, "Image capture failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            takePictureLauncher.launch(takePictureIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Beer Name") })
            OutlinedTextField(
                value = brewery,
                onValueChange = { brewery = it },
                label = { Text("Brewery") })
            OutlinedTextField(
                value = style,
                onValueChange = { style = it },
                label = { Text("Style") })
            OutlinedTextField(
                value = abv,
                onValueChange = { abv = it },
                label = { Text("ABV") })
            OutlinedTextField(
                value = volume,
                onValueChange = { volume = it },
                label = { Text("Volume") })
            OutlinedTextField(
                value = pictureUrl,
                onValueChange = { pictureUrl = it },
                label = { Text("Picture URL") })
            OutlinedTextField(
                value = howMany,
                onValueChange = { howMany = it },
                label = { Text("How Many") })



            Row(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onNavigateBack, modifier = Modifier.weight(1f)) {
                    Text("Back")
                }
                Button(
                    onClick = {
                        try {
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
                            onNavigateBack()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error adding beer: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Beer")
                }
            }
            Button(onClick = { dispatchTakePictureIntent() }, modifier = Modifier.padding().fillMaxWidth()) {
                Text("Capture Image")
            }
            imageUri?.let {
                val image: Painter = rememberAsyncImagePainter(model = it)
                Image(
                    painter = image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Button(
                    onClick = {
                        // Navigate to ImageDataScreen with the encoded imageUri
                        val encodedUri = Uri.encode(it.toString())
                        navController.navigate("${NavRoutes.ImageDataScreen.route}/$encodedUri")
                    },
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                ) {
                    Text("Navigate to Image Data Screen")
                }
            }
        }
    }
}

