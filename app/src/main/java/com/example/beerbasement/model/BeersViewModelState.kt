package com.example.beerbasement.model

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.beerbasement.repository.BeersRepository
import androidx.compose.runtime.State

class BeersViewModelState : ViewModel() {
   private val  repository = BeersRepository()
    var beersFlow: State<List<Beer>> = repository.beersFlow
    val errorMessage : State<String> = repository.errorMessageFlow

    init {
        reload()
    }

    fun reload() {
        repository.getBeers()

    }

    fun getBeersByUsername(username: String) {
        repository.getBeersByUsername(username)
    }

    fun clearBeers() {
        repository.beersFlow.value = emptyList()
    }

    fun addBeer(beer: Beer) {
        repository.addBeer(beer)
    }

    fun deleteBeer(beerId: Int) {
        repository.deleteBeer(beerId)
    }

    fun updateBeer(beerId: Int, beer: Beer) {
        repository.updateBeer(beerId, beer)
    }

    fun sortBeersByBrewery(ascending: Boolean) {
        repository.sortBeersByBrewery(ascending)
    }

    fun sortBeersByName(ascending: Boolean) {
        repository.sortBeersByName(ascending)
    }

    fun sortBeersByABV(ascending: Boolean) {
        repository.sortBeersByABV(ascending)
    }

    fun sortBeersByVolume(ascending: Boolean) {
        repository.sortBeersByVolume(ascending)
    }

    fun filterByTitle(title: String) {
        repository.filterByTitle(title)
    }

    fun navigateToUrlSite(context: Context, url: String) {
        repository.NavigateToUrlSite(context, url)
    }








}