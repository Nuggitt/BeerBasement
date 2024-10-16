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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerDetails(
    beer: Beer,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    signOut: () -> Unit = {},
    onUpdate: (Int, Beer) -> Unit = { beerId: Int, updatedBeer: Beer -> }
) {
    var title by remember { mutableStateOf(beer.name) }
    var brewery by remember { mutableStateOf(beer.brewery) }
    var style by remember { mutableStateOf(beer.style) }
    var abv by remember { mutableStateOf(beer.abv.toString()) }
    var volume by remember { mutableStateOf(beer.volume.toString()) }
    var pictureUrl by remember { mutableStateOf(beer.pictureUrl) }
    var howMany by remember { mutableStateOf(beer.howMany.toString()) }

    fun updateBeer() {
        val updatedBeer = Beer(
            id = beer.id,
            user = beer.user,
            brewery = brewery,
            name = title,
            style = style,
            abv = abv.toFloatOrNull() ?: 0f,
            volume = volume.toFloatOrNull() ?: 0f,
            pictureUrl = pictureUrl,
            howMany = howMany.toIntOrNull() ?: 0
        )

        onUpdate(beer.id, updatedBeer)
    }

    Scaffold(modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                title = { Text("Beer Details") },
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
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    updateBeer() // Call updateBeer whenever title changes
                },
                label = { Text("Beer Name") },
                modifier = Modifier.fillMaxWidth() // Fill max width for the OutlinedTextField
            )

            OutlinedTextField(
                value = brewery,
                onValueChange = {
                    brewery = it
                    updateBeer() // Call updateBeer whenever brewery changes
                },
                label = { Text("Brewery") },
                modifier = Modifier.fillMaxWidth() // Fill max width for the OutlinedTextField
            )

            OutlinedTextField(
                value = style,
                onValueChange = {
                    style = it
                    updateBeer() // Call updateBeer whenever style changes
                },
                label = { Text("Style") },
                modifier = Modifier.fillMaxWidth() // Fill max width for the OutlinedTextField
            )

            OutlinedTextField(
                value = abv,
                onValueChange = {
                    abv = it
                    updateBeer() // Call updateBeer whenever abv changes
                },
                label = { Text("ABV") },
                modifier = Modifier.fillMaxWidth() // Fill max width for the OutlinedTextField
            )

            OutlinedTextField(
                value = volume,
                onValueChange = {
                    volume = it
                    updateBeer() // Call updateBeer whenever volume changes
                },
                label = { Text("Volume") },
                modifier = Modifier.fillMaxWidth() // Fill max width for the OutlinedTextField
            )

            OutlinedTextField(
                value = pictureUrl,
                onValueChange = {
                    pictureUrl = it
                    updateBeer() // Call updateBeer whenever picture URL changes
                },
                label = { Text("Picture URL") },
                modifier = Modifier.fillMaxWidth() // Fill max width for the OutlinedTextField
            )

            OutlinedTextField(
                value = howMany,
                onValueChange = {
                    howMany = it
                    updateBeer() // Call updateBeer whenever how many changes
                },
                label = { Text("How Many") },
                modifier = Modifier.fillMaxWidth() // Fill max width for the OutlinedTextField
            )

            // Optional Back Button
            Button(onClick = { onNavigateBack() }) {
                Text("Back")
            }
        }
    }
}
