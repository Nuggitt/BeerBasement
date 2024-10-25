package com.example.beerbasement.screens

import android.content.res.Configuration
import android.inputmethodservice.Keyboard
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.beerbasement.model.Beer
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeerAdd(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    addBeer: (Beer) -> Unit = {},
    signOut: () -> Unit = { FirebaseAuth.getInstance().signOut() },
) {
    var title by rememberSaveable() { mutableStateOf("") }
    var brewery by rememberSaveable { mutableStateOf("") }
    var style by rememberSaveable { mutableStateOf("") }
    var abv by rememberSaveable { mutableStateOf("") }
    var volume by rememberSaveable { mutableStateOf("") }
    var pictureUrl by rememberSaveable { mutableStateOf("") }
    var howMany by rememberSaveable { mutableStateOf("") }
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser?.email ?: "Unknown"
    val orientation = LocalConfiguration.current.orientation

    Scaffold(modifier = modifier.fillMaxSize(),
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
                Column {
                    Row {
                        Button(onClick = onNavigateBack) {
                            Text("Back")
                        }
                        Button(onClick = {
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
                        }) {
                            Text("Add Beer")
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                modifier = modifier
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp),
                columns = GridCells.Fixed(2)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Beer Name") }
                    )
                }
                item {
                    OutlinedTextField(
                        value = brewery,
                        onValueChange = { brewery = it },
                        label = { Text("Brewery") }
                    )
                }
                item {
                    OutlinedTextField(
                        value = style,
                        onValueChange = { style = it },
                        label = { Text("Style") }
                    )
                }
                item {
                    OutlinedTextField(
                        value = abv,
                        onValueChange = { abv = it },
                        label = { Text("ABV") }
                    )
                }
                item {
                    OutlinedTextField(
                        value = volume,
                        onValueChange = { volume = it },
                        label = { Text("Volume") }
                    )
                }
                item {
                    OutlinedTextField(
                        value = pictureUrl,
                        onValueChange = { pictureUrl = it },
                        label = { Text("Picture URL") }
                    )
                }
                item {
                    OutlinedTextField(
                        value = howMany,
                        onValueChange = { howMany = it },
                        label = { Text("How Many") }
                    )
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

@Composable
@Preview(showBackground = true)
fun BeerAddPreview() {
    BeerAdd()
}