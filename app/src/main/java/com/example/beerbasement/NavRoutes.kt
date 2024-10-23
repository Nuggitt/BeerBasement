package com.example.beerbasement

sealed class NavRoutes(val route: String) {
    data object BeerList : NavRoutes("list")
    data object BeerAdd : NavRoutes("add")
    data object BeerDetails : NavRoutes("details")
    data object Login : NavRoutes("login")

}