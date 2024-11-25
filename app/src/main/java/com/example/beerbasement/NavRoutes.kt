package com.example.beerbasement

sealed class NavRoutes(val route: String) {
    data object BeerList : NavRoutes("list")
    data object BeerAdd : NavRoutes("add")
    data object BeerDetails : NavRoutes("details")
    data object Login : NavRoutes("login")
    object CameraScreen : NavRoutes("camera_screen")
    object ImageLabelingScreen : NavRoutes("image_labeling_screen/{savedUri}") {
        fun createRoute(savedUri: String) = "image_labeling_screen/$savedUri"
    }

}