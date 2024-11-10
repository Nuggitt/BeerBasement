package com.example.beerbasement.screens

import android.content.res.Configuration
import android.provider.ContactsContract.CommonDataKinds.Website
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Liquor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

import com.example.beerbasement.R
import com.example.beerbasement.model.Beer
import com.example.beerbasement.model.BeersViewModelState
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
    onDelete: (Int) -> Unit = {},
    sortByBrewery: (up: Boolean) -> Unit = {},
    sortByName: (up: Boolean) -> Unit = {},
    sortByABV: (up: Boolean) -> Unit = {},
    sortByVolume: (up: Boolean) -> Unit = {},
    filterByTitle: (title: String) -> Unit = {},
    navigateToUrlSite: (String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                ),
                title = {
                    Text(
                        "Welcome to BeerBasement ${user?.email ?: "Guest"}",
                        modifier = Modifier.padding(4.dp),
                        fontSize = 14.sp
                    )

                },
                actions = {
                    IconButton(
                        onClick = {
                            navigateToUrlSite("https://untappd.com/")
                        },

                        ) {
                        Icon(Icons.Filled.Liquor, contentDescription = "BeerBasement")
                    }

                    IconButton(onClick = { signOut() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")

                    }


                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 85.dp)
                    .padding()


            )


        },
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
            FloatingActionButton(
                shape = MaterialTheme.shapes.medium,
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

            if (user == null) {
                navigateToAuthentication()
            } else {



                BeerListPanel(
                    beers = beers,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize(),
                    errorMessage = errorMessage,
                    onBeerSelected = onBeerSelected,
                    userEmail = user.email ?: "",
                    onDelete = onDelete,
                    sortByBrewery = sortByBrewery,
                    sortByName = sortByName,
                    sortByABV = sortByABV,
                    sortByVolume = sortByVolume,
                    filterByTitle = filterByTitle,
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
    onDelete: (Int) -> Unit = {},
    sortByBrewery: (up: Boolean) -> Unit = {},
    sortByName: (up: Boolean) -> Unit = {},
    sortByABV: (up: Boolean) -> Unit = {},
    sortByVolume: (up: Boolean) -> Unit = {},
    filterByTitle: (title: String) -> Unit = {}


) {
    // Filter the list of beers based on the logged-in user's email
    val filteredBeers = beers.filter { it.user == userEmail }
    var sortBreweryAscending by rememberSaveable { mutableStateOf(true) }
    var sortNameAscending by rememberSaveable { mutableStateOf(true) }
    var sortABVAscending by rememberSaveable { mutableStateOf(true) }
    var sortVolumeAscending by rememberSaveable { mutableStateOf(true) }
    var titleFragment by rememberSaveable { mutableStateOf("") }
    val orientation = LocalConfiguration.current.orientation

    Column(modifier = modifier) {
        if (errorMessage.isNotEmpty()) { // Show error message only if it's not empty
            Text(text = "FEJL din øl liste er tom: $errorMessage", color = MaterialTheme.colorScheme.error)
        }
        Row {
            OutlinedTextField(
                value = titleFragment,
                onValueChange = {
                    titleFragment = it
                    filterByTitle(it)
                },
                label = { Text("Search Beer Name Or Brewery") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("SearchBeerNameOrBrewery"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
        }

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Modifier
                .weight(1f)
                .padding(8.dp)
                .height(48.dp) // Set a specific height for portrait
        } else {
            Modifier
                .weight(1f)
                .padding(8.dp)
                .height(56.dp) // Set a different height for landscape
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Button(
                onClick = {
                    sortBreweryAscending = !sortBreweryAscending
                    sortByBrewery(sortBreweryAscending)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = PaddingValues(12.dp)
            ) {
                Text(
                    text = "Sort by Brewery",
                    style = MaterialTheme.typography.labelSmall
                )
                Icon(
                    imageVector = if (sortBreweryAscending) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (sortBreweryAscending) "Sort Ascending" else "Sort Descending",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 4.dp)
                )

            }

            Button(
                onClick = {
                    sortNameAscending = !sortNameAscending
                    sortByName(sortNameAscending)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = PaddingValues(12.dp)
            ) {
                Text(
                    text = "Sort by Name",
                    style = MaterialTheme.typography.labelSmall
                )
                Icon(
                    imageVector = if (sortNameAscending) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (sortNameAscending) "Sort Ascending" else "Sort Descending",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 4.dp)
                )
            }

            Button(
                onClick = {
                    sortABVAscending = !sortABVAscending
                    sortByABV(sortABVAscending)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = PaddingValues(12.dp)
            ) {
                Text(
                    text = "Sort by ABV",
                    style = MaterialTheme.typography.labelSmall
                )
                Icon(
                    imageVector = if (sortABVAscending) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (sortABVAscending) "Sort Ascending" else "Sort Descending",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 4.dp),
                )
            }

            Button(
                onClick = {
                    sortVolumeAscending = !sortVolumeAscending
                    sortByVolume(sortVolumeAscending)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = PaddingValues(12.dp)

            ) {
                Text(
                    text = "Sort by Volume",
                    style = MaterialTheme.typography.labelSmall
                )
                Icon(
                    imageVector = if (sortVolumeAscending) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (sortVolumeAscending) "Sort Ascending" else "Sort Descending",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 4.dp)
                )
            }
        }


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
    onDelete: (Int) -> Unit = {}
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        onClick = { onBeerSelected(beer) },
        colors = CardDefaults.cardColors(
            MaterialTheme.colorScheme.primaryContainer,
        )
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
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,


                )
            AsyncImage(
                model = beer.pictureUrl,
                contentDescription = "Beer Image",
                modifier = Modifier
                    .size(64.dp)
                    .padding(8.dp)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium),

                )
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Icon",
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
                    .clickable { onDelete(beer.id) },
                tint = MaterialTheme.colorScheme.primary


            )
        }
    }
}

@Preview(showBackground = true, name = "BeerList Preview")
@Composable
fun PreviewBeerList() {
    val sampleBeers = listOf(
        Beer(id = 1, user = "test@example.com",  name = "Sample Beer 1", brewery = "Sample Brewery 1",  style = "some beer", abv = 5.0f, volume = 500f, pictureUrl = "https://example.com/image.jpg", howMany = 1),
        Beer(id = 2,  user = "test@example.com", name = "Sample Beer 2", brewery = "Sample Brewery 2", style = "some beer", abv = 6.5f, volume = 330f, pictureUrl = "https://example.com/image.jpg", howMany = 1),
    )
    BeerList(
        beers = sampleBeers,
        errorMessage = "",
        user = null,  // Assume guest user for preview
        signOut = {},
        navigateToAuthentication = {},
        onAdd = {},
        onDelete = {},
        sortByBrewery = {},
        sortByName = {},
        sortByABV = {},
        sortByVolume = {},
        filterByTitle = {},
        navigateToUrlSite = {}
    )
}

@Preview(showBackground = true, name = "BeerListPanel Preview")
@Composable
fun PreviewBeerListPanel() {
    val sampleBeers = listOf(
        Beer(id = 1,  user = "test@example.com", name = "Sample Beer 1", brewery = "Sample Brewery 1", style = "Some beer", abv =  5.0f, volume = 500f, pictureUrl = "https://example.com/image.jpg", howMany = 1),
        Beer(id = 2, user  = "test@example.com", name = "Sample Beer 2", brewery = "Sample Brewery 2", style = "some beer", abv =  6.5f, volume = 330f, pictureUrl = "https://example.com/image.jpg", howMany = 1),
    )
    BeerListPanel(
        beers = sampleBeers,
        errorMessage = "",
        onBeerSelected = {},
        userEmail = "test@example.com",
        onDelete = {},
        sortByBrewery = {},
        sortByName = {},
        sortByABV = {},
        sortByVolume = {},
        filterByTitle = {}
    )
}

@Preview(showBackground = true, name = "BeerItem Preview")
@Composable
fun PreviewBeerItem() {
    val sampleBeer = Beer(
        id = 1,
        user = "test@example.com",
        name = "Sample Beer",
        brewery = "Sample Brewery",
        style = "Some beer",
        abv = 5.0f,
        volume = 500f,
        pictureUrl = "https://example",
        howMany = 1

    )
    BeerItem(
        beer = sampleBeer,
        onBeerSelected = {},
        onDelete = {}
    )
}
