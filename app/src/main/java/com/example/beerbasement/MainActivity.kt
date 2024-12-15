package com.example.beerbasement

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.beerbasement.screens.ImageDataScreen
import com.example.beerbasement.ui.theme.BeerBasementTheme

class MainActivity : ComponentActivity() {
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap?
                imageBitmap?.let {
                    // Handle the captured image
                }
            } else {
                Toast.makeText(this, "Image capture failed", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            BeerBasementTheme {
                MainScreen(takePictureLauncher)
            }
        }
    }
}

@Composable
fun MainScreen(takePictureLauncher: ActivityResultLauncher<Intent>, modifier: Modifier = Modifier) {
    val viewModel: BeersViewModelState = viewModel()
    val navController = rememberNavController()
    val authenticationViewModel: AuthenticationViewModel = viewModel()
    val beers = viewModel.beersFlow.value
    val errorMessage = viewModel.errorMessage.value
    val user = authenticationViewModel.user
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = NavRoutes.Login.route) {
        composable(NavRoutes.Login.route) {
            Authentication(
                modifier = modifier,
                user = authenticationViewModel.user,
                message = authenticationViewModel.message,
                signIn = { email, password ->
                    authenticationViewModel.signIn(email, password)
                },
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
                    viewModel.updateBeer(
                        beerid,
                        updatedBeer
                    )
                },
                navigateToUrlSite = { url -> viewModel.navigateToUrlSite(context, url) }
            )
        }
        composable(NavRoutes.BeerAdd.route) {
            BeerAdd(
                modifier = modifier,
                onNavigateBack = { navController.popBackStack() },
                addBeer = { beer -> viewModel.addBeer(beer) },
                signOut = {
                    authenticationViewModel.signOut()
                    viewModel.clearBeers() // Clear beers on sign out
                },
                navController = navController
            )
        }
        composable(
            NavRoutes.ImageDataScreen.route + "/{imageUri}",
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri")
            ImageDataScreen(imageUri = imageUri)
        }
    }
}
