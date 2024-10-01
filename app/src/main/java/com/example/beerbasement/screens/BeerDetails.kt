package com.example.beerbasement.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.material3.Button
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
    var abv by remember { mutableStateOf(beer.abv.toString()) } // Convert to string for TextField
    var volume by remember { mutableStateOf(beer.volume.toString()) } // Convert to string for TextField
    var pictureUrl by remember { mutableStateOf(beer.pictureUrl) }
    var howMany by remember { mutableStateOf(beer.howMany.toString()) } // Convert to string for TextField

    // UI Layout
    Column(modifier = modifier) {
        TextField(value = title, onValueChange = { title = it }, label = { Text("Beer Name") })
        TextField(value = user, onValueChange = { user = it }, label = { Text("User") })
        TextField(value = brewery, onValueChange = { brewery = it }, label = { Text("Brewery") })
        TextField(value = style, onValueChange = { style = it }, label = { Text("Style") })
        TextField(value = abv, onValueChange = { abv = it }, label = { Text("ABV") })
        TextField(value = volume, onValueChange = { volume = it }, label = { Text("Volume") })
        TextField(value = pictureUrl, onValueChange = { pictureUrl = it }, label = { Text("Picture URL") })
        TextField(value = howMany, onValueChange = { howMany = it }, label = { Text("How Many") })

        // Optional Back Button
        Button(onClick = { onNavigateBack() }) {
            Text("Back")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BeerDetailsPreview() {
    BeerDetails(beer = Beer(1, "User", "Brewery", "Name", "Style", 5f, 500f, "https://www.google.com", 1))
}
