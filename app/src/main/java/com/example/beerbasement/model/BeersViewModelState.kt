package com.example.beerbasement.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.beerbasement.repository.BeersRepository
import androidx.compose.runtime.State

class BeersViewModelState : ViewModel() {
   private val  repository = BeersRepository()
    val beersFlow: State<List<Beer>> = repository.beersFlow
    val errorMessage : State<String> = repository.errorMessageFlow
    val reloadingFlow: State<Boolean> = repository.isLoadingBeers

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


}