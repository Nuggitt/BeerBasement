package com.example.beerbasement.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import com.example.beerbasement.model.Beer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerDetails(
    beer: Beer,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    var title by remember { mutableStateOf(beer.name) }
    var user by remember { mutableStateOf(beer.user) }
    var brewery by remember { mutableStateOf(beer.brewery) }
    var style by remember { mutableStateOf(beer.style) }
    var abv by remember { mutableStateOf(beer.abv) }
    var volume by remember { mutableStateOf(beer.volume) }
    var pictureUrl by remember { mutableStateOf(beer.pictureUrl) }
    var howMany by remember { mutableStateOf(beer.howMany) }

    Text(text = "Title: $title")
    Text(text = "User: $user")
    Text(text = "Brewery: $brewery")
    Text(text = "Style: $style")
    Text(text = "ABV: $abv")
    Text(text = "Volume: $volume")
    Text(text = "Picture URL: $pictureUrl")
    Text(text = "How Many: $howMany")
}

@Preview (showBackground = true)
@Composable
fun BeerDetailsPreview() {
    BeerDetails(beer = Beer(1, "User", "Brewery", "Name", "Style", 5f, 500f, "https://www.google.com", 1))
}