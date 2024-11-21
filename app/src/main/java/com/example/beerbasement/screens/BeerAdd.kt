package com.example.beerbasement.screens
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.beerbasement.model.Beer
import com.example.beerbasement.ui.theme.BeerBasementTheme
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerAdd(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    addBeer: (Beer) -> Unit = {},
    signOut: () -> Unit = { FirebaseAuth.getInstance().signOut() },
    navigateToImageLabelingScreen: (String) -> Unit = {}
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
    val orientation = LocalConfiguration.current.orientation


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
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Portrait Mode
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
                            addBeer(
                                Beer(
                                    user = currentUser,
                                    brewery = brewery,
                                    name = title,
                                    style = style,
                                    abv = abv.toFloat(),
                                    volume = volume.toFloat(),
                                    pictureUrl = pictureUrl,
                                    howMany = howMany.toInt()
                                )
                            )
                            onNavigateBack()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Beer")
                    }
                }
                // "Take a Photo" button
                Button(onClick = {
                    // Use a URI to navigate to the image labeling screen (if necessary)
                    val savedUri = "your_image_uri_here" // Replace with the actual URI after capturing image
                    navigateToImageLabelingScreen(savedUri) // Navigate to the image labeling screen
                }) {
                    Text("Take a Photo")
                }
            }
        } else {
            // Landscape Mode
            Box(modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Beer Name") })
                    }
                    item {
                        OutlinedTextField(
                            value = brewery,
                            onValueChange = { brewery = it },
                            label = { Text("Brewery") })
                    }
                    item {
                        OutlinedTextField(
                            value = style,
                            onValueChange = { style = it },
                            label = { Text("Style") })
                    }
                    item {
                        OutlinedTextField(
                            value = abv,
                            onValueChange = { abv = it },
                            label = { Text("ABV") })
                    }
                    item {
                        OutlinedTextField(
                            value = volume,
                            onValueChange = { volume = it },
                            label = { Text("Volume") })
                    }
                    item {
                        OutlinedTextField(
                            value = pictureUrl,
                            onValueChange = { pictureUrl = it },
                            label = { Text("Picture URL") })
                    }
                    item {
                        OutlinedTextField(
                            value = howMany,
                            onValueChange = { howMany = it },
                            label = { Text("How Many") })
                    }
                    item {
                        Row {
                            Button(onClick = onNavigateBack, modifier = Modifier.weight(1f)) {
                                Text("Back")
                            }
                            Button(
                                onClick = {
                                    addBeer(
                                        Beer(
                                            user = currentUser,
                                            brewery = brewery,
                                            name = title,
                                            style = style,
                                            abv = abv.toFloat(),
                                            volume = volume.toFloat(),
                                            pictureUrl = pictureUrl,
                                            howMany = howMany.toInt()
                                        )
                                    )
                                    onNavigateBack()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Add Beer")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "BeerAdd Preview")
@Composable
fun BeerAddPreview() {
    BeerBasementTheme {
        BeerAdd(
            modifier = Modifier,
            onNavigateBack = {},
            addBeer = {},
            signOut = {}
        )
    }
}









