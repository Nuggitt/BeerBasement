package com.example.beerbasement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.beerbasement.model.Beer
import com.example.beerbasement.model.BeersViewModelState
import com.example.beerbasement.screens.BeerList
import com.example.beerbasement.screens.BeerDetails
import com.example.beerbasement.ui.theme.BeerBasementTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    val beers = viewModel.beersFlow.value
    val errorMessage = viewModel.errorMessage.value

    NavHost(navController = navController, startDestination = NavRoutes.BeerList.route) {
        composable(NavRoutes.BeerList.route) {
            BeerList(
                modifier = modifier,
                beers = beers,
                errorMessage = errorMessage,
                onBeerSelected = { beer -> navController.navigate(NavRoutes.BeerDetails.route + "/${beer.id}") },
            )
        }
        composable(
            NavRoutes.BeerDetails.route + "/{beerId}",
            arguments = listOf(navArgument("beerId") { type = NavType.IntType })
        ) {
            navBackStackEntry ->
            val beerId = navBackStackEntry.arguments?.getInt("beerId")
            val beer = beers.find { it.id == beerId } ?: Beer(id = 0, user = "Unknown", brewery = "Unknown", name = "Unknown", style = "Unknown", abv = 0f, volume = 0f, pictureUrl = "", howMany = 0)
            BeerDetails(modifier = modifier, beer = beer, onNavigateBack = { navController.popBackStack() })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BeerBasementTheme {
        MainScreen()
    }
}