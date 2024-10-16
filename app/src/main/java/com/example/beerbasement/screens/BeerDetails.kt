package com.example.beerbasement.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.unit.dp
import com.example.beerbasement.model.Beer
import com.google.firebase.auth.FirebaseUser


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerDetails(
    beer: Beer,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    signOut: () -> Unit = {},
    onUpdate: (Int, Beer) -> Unit = { beerId: Int, updatedBeer: Beer -> },
    user: FirebaseUser? = null,
    navigateToAuthentication: () -> Unit = {},
) {
    var title by remember { mutableStateOf(beer.name) }
    var brewery by remember { mutableStateOf(beer.brewery) }
    var style by remember { mutableStateOf(beer.style) }
    var abv by remember { mutableStateOf(beer.abv.toString()) }
    var volume by remember { mutableStateOf(beer.volume.toString()) }
    var pictureUrl by remember { mutableStateOf(beer.pictureUrl) }
    var howMany by remember { mutableStateOf(beer.howMany.toString()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                title = { Text("Beer Details") },
                actions = {
                    IconButton(onClick = {
                        signOut()
                        navigateToAuthentication()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (user == null) {
                navigateToAuthentication()
            }

            // Informational Text
            Text(
                text = "You can update the fields below:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    onUpdate(beer.id, beer.copy(name = it)) // Update using onUpdate
                },
                label = { Text("Beer Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = brewery,
                onValueChange = {
                    brewery = it
                    onUpdate(beer.id, beer.copy(brewery = it)) // Update using onUpdate
                },
                label = { Text("Brewery") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = style,
                onValueChange = {
                    style = it
                    onUpdate(beer.id, beer.copy(style = it)) // Update using onUpdate
                },
                label = { Text("Style") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = abv,
                onValueChange = {
                    abv = it
                    onUpdate(beer.id, beer.copy(abv = it.toFloatOrNull() ?: 0f)) // Update using onUpdate
                },
                label = { Text("ABV") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = volume,
                onValueChange = {
                    volume = it
                    onUpdate(beer.id, beer.copy(volume = it.toFloatOrNull() ?: 0f)) // Update using onUpdate
                },
                label = { Text("Volume") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pictureUrl,
                onValueChange = {
                    pictureUrl = it
                    onUpdate(beer.id, beer.copy(pictureUrl = it)) // Update using onUpdate
                },
                label = { Text("Picture URL") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = howMany,
                onValueChange = {
                    howMany = it
                    onUpdate(beer.id, beer.copy(howMany = it.toIntOrNull() ?: 0)) // Update using onUpdate
                },
                label = { Text("How Many") },
                modifier = Modifier.fillMaxWidth()
            )

            // Optional Back Button
            Button(onClick = { onNavigateBack() }) {
                Text("Back")
            }
        }
    }
}


