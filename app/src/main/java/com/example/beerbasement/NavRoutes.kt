package com.example.beerbasement

import android.net.Uri

sealed class NavRoutes(val route: String) {
    data object BeerList : NavRoutes("list")
    data object BeerAdd : NavRoutes("add")
    data object BeerDetails : NavRoutes("details")
    data object Login : NavRoutes("login")
    object CameraScreen : NavRoutes("camera_screen")
    object ImageLabelingScreen : NavRoutes("image_labeling_screen/{savedUri}") {
        // Encode the URI to prevent issues with special characters
        fun createRoute(savedUri: String): String {
            val encodedUri = Uri.encode(savedUri)  // Encode the URI
            return "image_labeling_screen/$encodedUri"
        }
    }

}