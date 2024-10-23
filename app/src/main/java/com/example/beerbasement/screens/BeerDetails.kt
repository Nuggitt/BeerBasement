package com.example.beerbasement.screens

import android.content.res.Configuration
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BikeScooter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.example.beerbasement.model.Beer
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalWearMaterialApi::class)
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

    val swipeableState = rememberSwipeableState(0)
    val sizePx = with(LocalDensity.current) { 300.dp.toPx() }
    val anchors = mapOf(0f to 0, sizePx to 1)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val orientation = LocalConfiguration.current.orientation

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("You can also swipe to go back") },
                icon = { Icon(Icons.Filled.BikeScooter, contentDescription = "Swipe to go back") },
                onClick = {
                    try {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Try me in horizontal mode!",
                                actionLabel = "Dismiss",
                            )
                        }
                        snackbarHostState.currentSnackbarData?.dismiss()
                    } catch (
                        e: Exception
                    ) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Error: ${e.message}",
                                actionLabel = "Dismiss",
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ ->
                    FractionalThreshold(1f) // Increase threshold for easier swipe recognition
                },
                orientation = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Orientation.Horizontal
                } else {
                    Orientation.Vertical
                }
            ),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                ),
                title = {
                    Text(
                        "Beer Details",
                        fontSize = 14.sp,
                    )
                },
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

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                UpdateFieldsGrid(
                    beer,
                    onUpdate,
                    title,
                    brewery,
                    style,
                    abv,
                    volume,
                    pictureUrl,
                    howMany,
                    { title = it },
                    { brewery = it },
                    { style = it },
                    { abv = it },
                    { volume = it },
                    { pictureUrl = it },
                    { howMany = it },
                    onNavigateBack
                )
                Button(onClick = { onNavigateBack() }) { Text("Back") }

            } else {
                UpdateFieldsColumn(
                    beer,
                    onUpdate,
                    title,
                    brewery,
                    style,
                    abv,
                    volume,
                    pictureUrl,
                    howMany,
                    { title = it },
                    { brewery = it },
                    { style = it },
                    { abv = it },
                    { volume = it },
                    { pictureUrl = it },
                    { howMany = it },

                    )

            }
            Button(onClick = { onNavigateBack() }) { Text("Back") }

        }
    }

    if (swipeableState.offset.value > sizePx * 0.5) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        Text("Swipeable Content")
    }
}

@Composable
fun UpdateFieldsColumn(
    beer: Beer,
    onUpdate: (Int, Beer) -> Unit,
    title: String,
    brewery: String,
    style: String,
    abv: String,
    volume: String,
    pictureUrl: String,
    howMany: String,
    onTitleChange: (String) -> Unit,
    onBreweryChange: (String) -> Unit,
    onStyleChange: (String) -> Unit,
    onAbvChange: (String) -> Unit,
    onVolumeChange: (String) -> Unit,
    onPictureUrlChange: (String) -> Unit,
    onHowManyChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = title,
        onValueChange = {
            onTitleChange(it)
            onUpdate(beer.id, beer.copy(name = it))
        },
        label = { Text("Beer Name") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = brewery,
        onValueChange = {
            onBreweryChange(it)
            onUpdate(beer.id, beer.copy(brewery = it))
        },
        label = { Text("Brewery Name") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = style,
        onValueChange = {
            onStyleChange(it)
            onUpdate(beer.id, beer.copy(style = it))
        },
        label = { Text("Style") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = abv,
        onValueChange = {
            onAbvChange(it)
            onUpdate(beer.id, beer.copy(abv = it.toFloatOrNull() ?: 0f))
        },
        label = { Text("ABV") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = volume,
        onValueChange = {
            onVolumeChange(it)
            onUpdate(beer.id, beer.copy(volume = it.toFloatOrNull() ?: 0f))
        },
        label = { Text("Volume") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = pictureUrl,
        onValueChange = {
            onPictureUrlChange(it)
            onUpdate(beer.id, beer.copy(pictureUrl = it))
        },
        label = { Text("Picture URL") },
        modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
        value = howMany,
        onValueChange = {
            onHowManyChange(it)
            onUpdate(beer.id, beer.copy(howMany = it.toIntOrNull() ?: 0))
        },
        label = { Text("How Many") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun UpdateFieldsGrid(
    beer: Beer,
    onUpdate: (Int, Beer) -> Unit,
    title: String,
    brewery: String,
    style: String,
    abv: String,
    volume: String,
    pictureUrl: String,
    howMany: String,
    onTitleChange: (String) -> Unit,
    onBreweryChange: (String) -> Unit,
    onStyleChange: (String) -> Unit,
    onAbvChange: (String) -> Unit,
    onVolumeChange: (String) -> Unit,
    onPictureUrlChange: (String) -> Unit,
    onHowManyChange: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f) // Make grid take available space
        ) {
            items(
                listOf(
                    "Beer Name" to title,
                    "Brewery" to brewery,
                    "Style" to style,
                    "ABV" to abv,
                    "Volume" to volume,
                    "Picture URL" to pictureUrl,
                    "How Many" to howMany
                )
            ) { (label, value) ->
                val onValueChange: (String) -> Unit = { newValue ->
                    when (label) {
                        "Beer Name" -> {
                            onTitleChange(newValue)
                            onUpdate(beer.id, beer.copy(name = newValue))
                        }

                        "Brewery" -> {
                            onBreweryChange(newValue)
                            onUpdate(beer.id, beer.copy(brewery = newValue))
                        }

                        "Style" -> {
                            onStyleChange(newValue)
                            onUpdate(beer.id, beer.copy(style = newValue))
                        }

                        "ABV" -> {
                            val newAbv = newValue.toFloatOrNull() ?: 0f
                            onAbvChange(newValue)
                            onUpdate(beer.id, beer.copy(abv = newAbv))
                        }

                        "Volume" -> {
                            val newVolume = newValue.toFloatOrNull() ?: 0f
                            onVolumeChange(newValue)
                            onUpdate(beer.id, beer.copy(volume = newVolume))
                        }

                        "Picture URL" -> {
                            onPictureUrlChange(newValue)
                            onUpdate(beer.id, beer.copy(pictureUrl = newValue))
                        }

                        "How Many" -> {
                            val newHowMany = newValue.toIntOrNull() ?: 0
                            onHowManyChange(newValue)
                            onUpdate(beer.id, beer.copy(howMany = newHowMany))
                        }
                    }
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }


        Button(onClick = { onNavigateBack() }) {
            Text("Back")
        }
    }
}




