package com.example.beerbasement

import android.net.Uri

sealed class NavRoutes(val route: String) {
    data object BeerList : NavRoutes("list")
    data object BeerAdd : NavRoutes("add")
    data object BeerDetails : NavRoutes("details")
    data object Login : NavRoutes("login")
    object ImageDataScreen : NavRoutes("imageData/{photoUri}") {
        fun createRoute(photoUri: String): String = "imageData/$photoUri"
    }

}