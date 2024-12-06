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
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
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
        // Label Recognition: Check if it's a drink or beverage
        val drinkKeywords = listOf(
            "Drink", "Beverage", "Alcohol", "Beer", "Bottle", "Can", "Glass", "Mug", "Pint", "Pitcher", "Keg",
            "Liquid", "Fluid", "Tin", "Glass Bottle", "Label", "Logo", "Alcoholic Drink"
        )
        val isDrink = recognizedLabels.split(",")
            .map { it.trim() }
            .any { label -> drinkKeywords.any { label.contains(it, ignoreCase = true) } }


        // Only proceed with autofill if it's a drink
        if (isDrink) {
            // Filter brewery
            val knownBreweries = listOf(
                "Carlsberg", "Tuborg", "Heineken", "Budweiser", "Corona", "Guinness", "Stella Artois", "Amstel", "Beck's",
                "Coors", "Miller", "Pilsner Urquell", "Peroni", "Sierra Nevada", "Newcastle", "Schneider Weisse", "Lagunitas",
                // Add as many as needed
            )

            // Check if any recognized logo matches a known brewery
            if (recognizedLogos.isNotEmpty()) {
                val matchedBrewery = knownBreweries.firstOrNull { breweryName ->
                    recognizedLogos.contains(breweryName, ignoreCase = true)
                }
                if (matchedBrewery != null) {
                    brewery = matchedBrewery
                }
            }

            // Extract Beer Name & ABV
            if (recognizedText.isNotEmpty()) {
                // ABV Pattern: Matches values like "5%", "ABV 5.0%", etc.
                val cleanedText = recognizedText.replace(Regex("[^\\x00-\\x7F]+"), "")

                // ABV Pattern: Matches values like "5%", "ABV 5.0%", "ALK 4.6", etc.
                val abvPattern = Regex("(?:ABV|ALK\\.|VOL)[\\s:]*([\\d]{1,2}[.,]?[\\d]{0,2})\\s?%?")

                // Log the cleaned recognized text for debugging
                Log.d("ABV Debug", "Cleaned Recognized Text: $cleanedText")

                // Try to match the ABV pattern
                val abvMatch = abvPattern.find(cleanedText)

                // Log the ABV match attempt
                Log.d("ABV Debug", "ABV Pattern: $abvPattern")

                if (abvMatch != null) {
                    // Extract ABV and replace comma with dot if necessary
                    abv = abvMatch.groupValues[1].replace(",", ".")
                    // Log the extracted ABV
                    Log.d("ABV Debug", "Extracted ABV: $abv")
                } else {
                    // Log if no match was found
                    Log.d("ABV Debug", "No ABV found in the cleaned recognized text")
                }

                // Volume Pattern: Matches "500ml", "50cl", "16oz", etc.
                val volumePattern = Regex("""(?i)(\d{1,3}([.,]?\d{1,2})?)\s?(ml|cl|oz|l|mL|fl\.?oz|fluid\s?oz)""")
                val volumeMatch = volumePattern.find(recognizedText)
                volume = volumeMatch?.groupValues?.get(1)?.trim() ?: "" // Extract volume value

                // Extract Beer Title (based on known beer descriptors)
                val knownBeerTitles = listOf(
                    "Classic", "Nordic", "Red", "Gold", "Light", "Dark", "Amber", "Blonde", "Black", "Strong",
                    "Ice", "Premium", "Special", "Crisp", "Bitter", "Rich", "Sweet", "Smoked", "Golden",
                    "Ruby", "Draft", "Belgian", "American", "Wheat", "Pilsner", "IPA", "Craft", "Hazy", "Lager", "Ale"
                )
                title = knownBeerTitles.find { it -> recognizedText.contains(it, ignoreCase = true) } ?: recognizedText

                // Extract Beer Style (e.g., IPA, Stout, Lager)
                val knownBeerStyles = listOf(
                    "IPA", "Stout", "Pilsner", "Lager", "Ale", "Porter", "Wheat", "Bock", "Saison", "Blonde", "Amber", "Pale Ale", "Slow Beer"
                )
                style = knownBeerStyles.find { style -> recognizedText.contains(style, ignoreCase = true) } ?: ""  // Default to empty if not found
            }
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





