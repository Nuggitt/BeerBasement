package com.example.beerbasement

import android.net.Uri

sealed class NavRoutes(val route: String) {
    data object BeerList : NavRoutes("list")
    object BeerAdd : NavRoutes("beerAdd/{imageUri}/{recognizedLogos}/{recognizedText}/{recognizedLabels}") {
        fun createRoute(
            imageUri: String,
            recognizedLogos: String,
            recognizedText: String,
            recognizedLabels: String
        ): String {
            return "beerAdd/$imageUri/$recognizedLogos/$recognizedText/$recognizedLabels"
        }
    }
    data object BeerDetails : NavRoutes("details")
    data object Login : NavRoutes("login")
    object ImageDataScreen : NavRoutes("imageData/{photoUri}") {
        fun createRoute(photoUri: String): String = "imageData/$photoUri"
    }

}