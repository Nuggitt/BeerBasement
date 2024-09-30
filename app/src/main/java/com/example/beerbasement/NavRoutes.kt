package com.example.beerbasement

sealed class NavRoutes(val route: String) {
    data object BeerList : NavRoutes("beerlist")
    data object Add : NavRoutes("add")
    data object Edit : NavRoutes("edit")
    data object Details : NavRoutes("details")
}