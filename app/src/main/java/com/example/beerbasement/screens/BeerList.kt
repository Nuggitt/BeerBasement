package com.example.beerbasement.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.beerbasement.model.Beer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerList(
    beers: List<Beer>,
    errorMessage: String,
    modifier: Modifier = Modifier,
    onBeerSelected: (Beer) -> Unit = {},

) {
    //val scaffoldState = rememberScaffoldState()
    Scaffold(modifier = modifier,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text("Book list") })
        },
        // TODO in landscape layout, half the button is outside the screen
        // something relating to rememberScaffoldState?
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                onClick = { /* TODO */ },
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
            }
        }) { innerPadding ->
        BeerListPanel(
            beers = beers,
            modifier = Modifier.padding(innerPadding),
            errorMessage = errorMessage,

        )
    }
}

@Composable
private fun BeerListPanel(
    beers: List<Beer>,
    modifier: Modifier = Modifier,
    errorMessage: String,
) {
    Column(modifier = modifier) {
        if (errorMessage.isEmpty()) {
            Text(text = "Problem: $errorMessage")
        }

    }
}

@Preview(showBackground = true)
@Composable
fun BeerListPreview() {
    BeerList(
        beers = listOf(
            Beer(1, "User 1", "Brewery 1", "Beer 1", "Style 1", 5.0, 500, "http://example.com/beer1.png", 10),
            Beer(2, "User 2", "Brewery 2", "Beer 2", "Style 2", 5.0, 500, "http://example.com/beer1.png", 10),
            Beer(3, "User 3", "Brewery 3", "Beer 3", "Style 3", 5.0, 500, "http://example.com/beer1.png", 10),
        ),
        errorMessage = "Error",
    )
}

