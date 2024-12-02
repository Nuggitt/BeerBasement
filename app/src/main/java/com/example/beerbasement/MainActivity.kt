package com.example.beerbasement

import ImageDataScreen
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.beerbasement.model.AuthenticationViewModel
import com.example.beerbasement.model.Beer
import com.example.beerbasement.model.BeersViewModelState
import com.example.beerbasement.screens.Authentication
import com.example.beerbasement.screens.BeerAdd
import com.example.beerbasement.screens.BeerDetails
import com.example.beerbasement.screens.BeerList
import com.example.beerbasement.ui.theme.BeerBasementTheme
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge UI
        enableEdgeToEdge()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up the UI
        setContent {
            BeerBasementTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val viewModel: BeersViewModelState = viewModel()
    val navController = rememberNavController()
    val authenticationViewModel: AuthenticationViewModel = viewModel()
    val beers = viewModel.beersFlow.value
    val errorMessage = viewModel.errorMessage.value
    val user = authenticationViewModel.user
    val context = LocalContext.current





    // Navigation setup for different screens
    NavHost(navController = navController, startDestination = NavRoutes.Login.route) {
        composable(NavRoutes.Login.route) {
            Authentication(
                modifier = modifier,
                user = authenticationViewModel.user,
                message = authenticationViewModel.message,
                signIn = { email, password -> authenticationViewModel.signIn(email, password) },
                register = { email, password -> authenticationViewModel.register(email, password) },
                navigateToNextScreen = { navController.navigate(NavRoutes.BeerList.route) }
            )
        }

        composable(NavRoutes.BeerList.route) {
            LaunchedEffect(user) {
                user?.email?.let {
                    viewModel.getBeersByUsername(it) // Fetch beers for the specific user
                } ?: run {
                    viewModel.clearBeers() // Clear beers if no user is logged in
                }
            }
            BeerList(
                modifier = modifier.fillMaxSize(),
                beers = beers,
                errorMessage = errorMessage,
                onBeerSelected = { beer -> navController.navigate(NavRoutes.BeerDetails.route + "/${beer.id}") },
                user = user,
                signOut = {
                    authenticationViewModel.signOut()
                    viewModel.clearBeers() // Clear beers on sign out
                },
                navigateToAuthentication = {
                    navController.popBackStack(NavRoutes.Login.route, inclusive = false)
                },
                onAdd = { navController.navigate(NavRoutes.BeerAdd.route) },
                onDelete = { beerId: Int -> viewModel.deleteBeer(beerId) },
                sortByBrewery = { viewModel.sortBeersByBrewery(ascending = it) },
                sortByName = { viewModel.sortBeersByName(ascending = it) },
                sortByABV = { viewModel.sortBeersByABV(ascending = it) },
                sortByVolume = { viewModel.sortBeersByVolume(ascending = it) },
                filterByTitle = { viewModel.filterByTitle(it) },
                navigateToUrlSite = { url -> viewModel.navigateToUrlSite(context, url) }
            )
        }

        composable(
            NavRoutes.BeerDetails.route + "/{beerId}",
            arguments = listOf(navArgument("beerId") { type = NavType.IntType })
        ) { navBackStackEntry ->
            val beerId = navBackStackEntry.arguments?.getInt("beerId")
            val beer = beers.find { it.id == beerId } ?: Beer(
                id = 0,
                user = "Unknown",
                brewery = "Unknown",
                name = "Unknown",
                style = "Unknown",
                abv = 0f,
                volume = 0f,
                pictureUrl = "",
                howMany = 0
            )
            BeerDetails(
                modifier = modifier,
                beer = beer,
                onNavigateBack = { navController.popBackStack() },
                signOut = {
                    authenticationViewModel.signOut()
                    viewModel.clearBeers() // Clear beers on sign out
                },
                onUpdate = { beerid: Int, updatedBeer: Beer ->
                    viewModel.updateBeer(beerid, updatedBeer)
                },
                navigateToUrlSite = { url -> viewModel.navigateToUrlSite(context, url) }
            )
        }

        composable(NavRoutes.BeerAdd.route) {
            BeerAdd(
                modifier = Modifier,
                onNavigateBack = { navController.popBackStack() },
                addBeer = { beer -> viewModel.addBeer(beer) },
                navController = navController
            )
        }

        composable(
            route = NavRoutes.ImageDataScreen.route,
            arguments = listOf(
                navArgument("photoUri") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { navBackStackEntry ->
            val photoUriString = navBackStackEntry.arguments?.getString("photoUri")
            val photoUri = photoUriString?.let { Uri.parse(it) }

            if (photoUri != null) {
                // Pass the parsed URI to ImageDataScreen
                ImageDataScreen(imageUri = photoUri)
            } else {
                // Handle missing or invalid URI
                Text(
                    text = "Invalid or missing photo URI",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

private fun startCameraIntent(context: Context) {
    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    context.startActivity(cameraIntent)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BeerBasementTheme {
        MainScreen()
    }
}
