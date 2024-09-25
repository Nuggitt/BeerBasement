package com.example.beerbasement.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class BeersViewModelState : ViewModel() {
    var beers by mutableStateOf<List<Beer>>(listOf())
        private set

    fun addBeer(beer: Beer) {
        beers = beers + beer
    }

    fun removeBeer(beer: Beer) {
        beers = beers - beer
    }




}