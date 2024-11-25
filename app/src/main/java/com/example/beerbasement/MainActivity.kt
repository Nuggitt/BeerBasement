package com.example.beerbasement

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.beerbasement.model.AuthenticationViewModel
import com.example.beerbasement.model.Beer
import com.example.beerbasement.model.BeersViewModelState
import com.example.beerbasement.screens.*
import com.example.beerbasement.ui.theme.BeerBasementTheme
import java.io.File

class MainActivity : ComponentActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val context = this // Context for the navigation

        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }

        setContent {
            BeerBasementTheme {
                MainScreen()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, initialize camera
                setContent {
                    BeerBasementTheme {
                        InitializeCamera()
                    }
                }
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun InitializeCamera() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(context) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = androidx.camera.core.Preview.Builder().build()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Camera setup failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(context))
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val photoFile = File(context.filesDir, "photo.jpg")

    LaunchedEffect(context) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = androidx.camera.core.Preview.Builder().build()
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageCapture
                    )
                } catch (e: Exception) {
                    Toast.makeText(context, "Kameraopsætning fejlede: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }, ContextCompat.getMainExecutor(context))
        } else {
            Toast.makeText(context, "Kamera-tilladelse kræves", Toast.LENGTH_SHORT).show()
        }
    }

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
                modifier = modifier
                    .fillMaxSize(),
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
                navController = navController,
            )
        }

        composable(NavRoutes.CameraScreen.route) {
            CameraScreen { capturedUri ->
                // Navigate to ImageLabelingScreen with the captured URI
                navController.navigate(
                    NavRoutes.ImageLabelingScreen.createRoute(capturedUri.toString())
                )
            }
        }

        // New ImageLabelingScreen destination
        composable(
            route = NavRoutes.ImageLabelingScreen.route,
            arguments = listOf(navArgument("savedUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val savedUriString = backStackEntry.arguments?.getString("savedUri")
            if (savedUriString != null) {
                ImageLabelingScreen(savedUri = Uri.parse(savedUriString))
            }
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