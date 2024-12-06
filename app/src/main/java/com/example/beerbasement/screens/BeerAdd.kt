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
                "Sam Adams", "BrewDog", "Stone Brewing", "Foster's", "Dos Equis", "Tsingtao", "Asahi", "Kirin", "Sapporo",
                "Bud Light", "Michelob", "Yuengling", "Carling", "Bitburger", "Sapporo", "Kronenbourg 1664", "Harpoon Brewery",
                "Founders Brewing", "Anchor Steam", "Victory Brewing", "Brooklyn Brewery", "Maui Brewing", "Lagunitas Brewing",
                "Deschutes Brewery", "Dogfish Head", "Firestone Walker", "Ballast Point", "BrewDog", "Stone Brewing", "Funkwerks",
                "Rheingold", "Coca-Cola Brewery", "Miller Lite", "Veltins", "Grolsch", "Warsteiner", "Schloss Eggenberg", "Hoegaarden",
                "Chimay", "Duvel", "La Trappe", "Trappistes Rochefort", "Westmalle", "Westvleteren", "Maredsous", "Kwok",
                "Ommegang Brewery", "Troegs Brewing", "Green Flash Brewing", "Oskar Blues", "Deschutes", "Left Hand Brewing",
                "SweetWater Brewing", "Bell's Brewery", "Saranac Brewing", "Great Lakes Brewing", "BrewDog", "Kona Brewing",
                "Funky Buddha Brewery", "Founders Brewing", "Rheingold", "Allagash Brewing", "21st Amendment Brewery", "Big Ditch Brewing",
                "Revolution Brewing", "New Belgium Brewing", "Stone Brewing", "Blue Moon Brewing", "Laughing Dog Brewing",
                "Hoegaarden Blanc", "Kronenbourg Blanc" ,"Bavaria", "La Chouffe", "Leffe", "Duvel", "Rochefort", "Chimay", "St. Bernardus", "Wittekop", "Paulaner",
                "Spaten", "Warsteiner", "Veltins", "Grolsch", "Carlsberg", "Czechvar", "Staropramen", "Baltika", "Žatec",
                "Pilsner Urquell", "Brouwerij Westmalle", "Brouwerij De Halve Maan", "Brouwerij St. Bernardus", "Brouwerij Duvel Moortgat",
                "Gösser", "Stiegl", "Ayinger", "Erdinger", "Krombacher", "Hofbräu München", "Augustiner", "Franziskaner",
                "BrewDog", "Brouwerij De Molen", "Brouwerij 3 Fonteinen", "Schneider Weisse", "DAB", "Zywiec", "Lindemans",
                "Brouwerij Grolsch", "Brouwerij Hoegaarden", "Brouwerij Liefmans", "Brouwerij Chimay", "Brouwerij Orval"
            ) // Add as many as needed

            if (recognizedLogos.isNotEmpty()) {
                val matchedBrewery = knownBreweries.firstOrNull { breweryName ->
                    recognizedLogos.contains(breweryName, ignoreCase = true)
                }
                if (matchedBrewery != null) {
                    brewery = matchedBrewery
                }
            }

            // Beer Name & ABV
            if (recognizedText.isNotEmpty()) {
                // Define ABV Pattern (matches values with % symbol after the number)
                val abvPattern = Regex("""(\d+[.,]?\d*)%\s*""")

                // Remove any characters that are not relevant to ABV or numbers
                val sanitizedText = recognizedText.replace(Regex("[^\\w\\s,%.]"), "")
                Log.d("Sanitized Text", sanitizedText)  // Log the sanitized text

                // Try to match the ABV pattern first
                val abvMatch = abvPattern.find(sanitizedText)
                if (abvMatch != null) {
                    abv = abvMatch.groupValues[1].replace(",", ".")  // Replace comma with dot if necessary
                    Log.d("ABV Debug", "Extracted ABV: $abv")
                } else {
                    Log.d("ABV Debug", "No ABV found in the sanitized text")
                }

                // Container Volume Pattern (e.g., 50cl, 500ml, 16oz, etc.)
                val volumePattern = Regex("""(\d+(\.\d+)?)\s*(cl|ml|oz|l)""") // Matches "50cl", "500ml", "16oz", etc.
                val volumeMatch = volumePattern.find(recognizedText)
                volume = volumeMatch?.groupValues?.get(1)?.trim() ?: "" // Extract volume value

                // Filter out ABV and unwanted characters (numbers, % symbols)
                val textWithoutAbv = recognizedText.replace(abvPattern, "").replace(volumePattern, "").trim()

                // List of known beer titles/descriptors (add more as needed)
                val knownBeerTitles = listOf(
                    "Classic", "Nordic", "Red", "Gold", "Light", "Dark", "Amber", "Blonde", "Black", "Strong",
                    "Ice", "Premium", "Special", "Crisp", "Bitter", "Rich", "Sweet", "Smoked", "Golden",
                    "Ruby", "Draft", "Belgian", "Hazy", "Lush", "Mild", "Brewed", "Pale", "Spicy", "Chilled",
                    "Citrus", "Cranberry", "Summer", "Winter", "Festive", "Old", "Mango", "Pineapple", "Cherry",
                    "Citrus", "Platinum", "Ginger", "Session", "Craft", "Barley", "Wheat", "Saison", "Rye",
                    "Cider", "Hop", "Lime", "Mango", "Oaked", "Maple", "Spiced", "Mango", "Pine", "Blackberry",
                    "Weißbier", "Hefeweizen"
                )

                // Try to match recognized text to a known beer title (or descriptor)
                val matchedTitle = knownBeerTitles.firstOrNull { descriptor ->
                    // Check if the descriptor is a full word (add boundaries to prevent partial matching)
                    val regex = Regex("\\b$descriptor\\b", RegexOption.IGNORE_CASE)
                    regex.containsMatchIn(textWithoutAbv)
                }

                title = matchedTitle ?: textWithoutAbv // Fallback to textWithoutAbv if no match is found

                // If a known beer title is found, use it as the beer title
                title = matchedTitle ?: textWithoutAbv // If no match, fall back to the filtered text

                // Attempt to extract beer style (e.g., IPA, Stout)
                val knownStyles = listOf(
                    "IPA", "Stout", "Lager", "Pale Ale", "Porter", "Pilsner", "Wheat", "Sour", "Malt Lager",
                    "Blonde Ale", "Brown Ale", "Amber Ale", "Belgian Ale", "Hefeweizen", "Weißbier", "Hazy IPA",
                    "Imperial Stout", "Witbier", "Belgian Dubbel", "Belgian Tripel", "Barleywine", "Berliner Weisse",
                    "Cider", "Session IPA", "Saison", "Bock", "Maibock", "Rauchbier", "Rye Beer", "Cream Ale",
                    "Fruit Beer", "Citrus IPA", "Milk Stout", "Double IPA", "New England IPA", "Schwarzbier",
                    "Blonde Ale", "Red Ale", "Golden Ale", "Lambic", "Amber Lager", "Imperial IPA", "European Lager",
                )

                val matchedStyle = knownStyles.firstOrNull { style ->
                    textWithoutAbv.contains(style, ignoreCase = true)
                }

                // Save the detected beer style (if any)
                style = matchedStyle ?: ""
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





