package com.example.beerbasement

sealed class NavRoutes(val route: String) {
    data object BeerList : NavRoutes("list")
    data object Add : NavRoutes("add")
    data object Edit : NavRoutes("edit")
    data object BeerDetails : NavRoutes("details")
    data object Login : NavRoutes("login")
    data object Welcome : NavRoutes("welcome")
}