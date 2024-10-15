package com.example.beerbasement.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.beerbasement.model.Beer
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerList(
    beers: List<Beer>,
    errorMessage: String,
    modifier: Modifier = Modifier,
    onBeerSelected: (Beer) -> Unit = {},
    user: FirebaseUser? = null,
    signOut: () -> Unit = {},
    navigateToAuthentication: () -> Unit = {},
    onAdd: () -> Unit = {},
    onDelete: (Int) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text("Welcome to BeerBasement") },
                actions = {
                    IconButton(onClick = { signOut() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                onClick = { onAdd() },
                containerColor = MaterialTheme.colorScheme.secondary,
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Check if user is null and navigate if needed
            if (user == null) {
                navigateToAuthentication()
            } else {
                // Display a welcome message if user is logged in
                Text(
                    "Welcome ${user.email ?: "unknown"}",
                    modifier = Modifier.padding(16.dp) // This can be adjusted
                )

                // Call the BeerListPanel function to display beers, passing the user's email
                BeerListPanel(
                    beers = beers,
                    modifier = Modifier
                        .padding(horizontal = 16.dp) // Only horizontal padding for the panel
                        .fillMaxSize(), // Ensure it takes the available space
                    errorMessage = errorMessage,
                    onBeerSelected = onBeerSelected,
                    userEmail = user.email ?: "",
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun BeerListPanel(
    beers: List<Beer>,
    modifier: Modifier = Modifier,
    errorMessage: String,
    onBeerSelected: (Beer) -> Unit = {},
    userEmail: String, // Pass the user's email to filter beers
    onDelete: (Int) -> Unit = {}
) {
    // Filter the list of beers based on the logged-in user's email
    val filteredBeers = beers.filter { it.user == userEmail }

    Column(modifier = modifier) {
        if (errorMessage.isNotEmpty()) { // Show error message only if it's not empty
            Text(text = "Problem: $errorMessage", color = MaterialTheme.colorScheme.error)
        }

        var beerTitle by remember { mutableStateOf("") }
        Row {
            OutlinedTextField(
                value = beerTitle,
                onValueChange = { /* Handle beer search here */ },
                label = { Text("Search Beer Title") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            Button(
                onClick = {
                    // Implement search logic here
                },
                modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 10.dp),
                shape = MaterialTheme.shapes.extraSmall,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search, contentDescription = "Search Icon"
                )
                Text("Search")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Text(
                text = "Sort By Name",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )
            Text(
                text = "Name",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )
            Text(
                text = "Volume",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )
            Text(
                text = "ABV",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )
        }

        val orientation = LocalConfiguration.current.orientation
        val columns = if (orientation == Configuration.ORIENTATION_PORTRAIT) 1 else 2
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize() // Ensure it takes up available space
        ) {
            items(filteredBeers) { beer -> // Only show filtered beers
                BeerItem(
                    beer = beer, onBeerSelected = onBeerSelected, onDelete = { onDelete(beer.id) }
                )
            }
        }
    }
}

@Composable
private fun BeerItem(
    beer: Beer,
    modifier: Modifier = Modifier,
    onBeerSelected: (Beer) -> Unit = {},
    onDelete: (Int) -> Unit = {}) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth(),
        onClick = { onBeerSelected(beer) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
                    .wrapContentHeight(),
                text = "${beer.id}: ${beer.brewery}: ${beer.name} \n    ABV: ${beer.abv} Volume: ${beer.volume}",
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Icon",
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
                    .clickable { onDelete(beer.id) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BeerListPreview() {
    MaterialTheme {
        BeerList(
            beers = listOf(
                Beer(
                    user = "User 1",
                    brewery = "Brewery 1",
                    name = "Beer 1",
                    style = "Style 1",
                    abv = 5.0f,
                    volume = 500f,
                    pictureUrl = "http://example.com/beer1.png",
                    howMany = 10
                ),
                Beer(
                    user = "User 2",
                    brewery = "Brewery 2",
                    name = "Beer 2",
                    style = "Style 2",
                    abv = 4.5f,
                    volume = 330f,
                    pictureUrl = "http://example.com/beer2.png",
                    howMany = 5
                ),
                Beer(
                    user = "User 3",
                    brewery = "Brewery 3",
                    name = "Beer 3",
                    style = "Style 3",
                    abv = 6.0f,
                    volume = 750f,
                    pictureUrl = "http://example.com/beer3.png",
                    howMany = 8
                )
            ),
            errorMessage = "No errors",
        )
    }
}