package com.example.beerbasement.repository

import BeerBasementService
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.beerbasement.model.Beer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BeersRepository {
    private val baseUrl = "https://anbo-restbeer.azurewebsites.net/api/"
    private val beerBasementService: BeerBasementService
    val beersFlow: MutableState<List<Beer>> = mutableStateOf(listOf())
    val isLoadingBeers = mutableStateOf(false)
    val errorMessageFlow = mutableStateOf("")

    init {
        val build: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        beerBasementService = build.create(BeerBasementService::class.java)
        getBeers()
    }

    fun getBeers() {
        isLoadingBeers.value = true
        beerBasementService.GetAllBeers().enqueue(object : Callback<List<Beer>> {
            override fun onResponse(call: Call<List<Beer>>, response: Response<List<Beer>>) {
                if (response.isSuccessful) {
                    Log.d("APPLE", response.body().toString())
                    val beerList: List<Beer>? = response.body()
                    beersFlow.value = beerList ?: emptyList()
                    errorMessageFlow.value = ""
                } else {
                    val message = response.code().toString() + " : " + response.message()
                    errorMessageFlow.value = message
                    Log.d("FEJLFEJLFEJL", message)
                }
            }

            override fun onFailure(call: Call<List<Beer>>, t: Throwable) {
                isLoadingBeers.value = false
                val message = t.message ?: "No connection to back-end"
                errorMessageFlow.value = message
                Log.d("APPLE", message)
            }
        })
    }

    fun getBeersByUsername(username: String) {
        isLoadingBeers.value = true

        // Call the endpoint using the username directly
        beerBasementService.getBeersByUsername(username).enqueue(object : Callback<List<Beer>> {
            override fun onResponse(call: Call<List<Beer>>, response: Response<List<Beer>>) {
                isLoadingBeers.value = false // Set loading to false after response
                if (response.isSuccessful) {
                    val beers: List<Beer>? = response.body()
                    beersFlow.value = beers ?: emptyList()
                    errorMessageFlow.value = ""
                } else {
                    val message = "${response.code()} : ${response.message()}"
                    errorMessageFlow.value = message
                    Log.d("ERROR", message)
                }
            }

            override fun onFailure(call: Call<List<Beer>>, t: Throwable) {
                isLoadingBeers.value = false
                val message = t.message ?: "No connection to back-end"
                errorMessageFlow.value = message
                Log.d("ERROR", message)
            }
        })
    }

    fun addBeer(beer: Beer) {
        beerBasementService.addBeer(beer).enqueue(object : Callback<Beer> {
            override fun onResponse(call: Call<Beer>, response: Response<Beer>) {
                if (response.isSuccessful) {
                    Log.d("APPLE", "Added beer" + response.body())
                    getBeers()
                } else {
                    val message = response.code().toString() + " : " + response.message()
                    errorMessageFlow.value = message
                    Log.d("ERROR", message)
                }
            }

            override fun onFailure(call: Call<Beer>, t: Throwable) {
                val message = t.message ?: "No connection to back-end"
                errorMessageFlow.value = message
                Log.d("APPLE", message)
            }

        })
    }
    
    fun deleteBeer(beerId: Int) {
        beerBasementService.deleteBeer(beerId).enqueue(object : Callback<Beer> {
            override fun onResponse(call: Call<Beer>, response: Response<Beer>) {
                if (response.isSuccessful) {
                    Log.d("APPLE", "Deleted beer")
                    getBeers()
                } else {
                    val message = response.code().toString() + " : " + response.message()
                    errorMessageFlow.value = message
                    Log.d("ERROR", message)
                }
            }

            override fun onFailure(call: Call<Beer>, t: Throwable) {
                val message = t.message ?: "No connection to back-end"
                errorMessageFlow.value = message
                Log.d("APPLE", message)
            }
        })
    }

    fun updateBeer(beerId: Int, beer: Beer) {
        // Use beerId for the update call
        beerBasementService.updateBeer(beerId, beer).enqueue(object : Callback<Beer> {
            override fun onResponse(call: Call<Beer>, response: Response<Beer>) {
                if (response.isSuccessful) {
                    Log.d("APPLE", "Updated beer")
                    getBeers() // Fetch updated list of beers
                } else {
                    val message = "${response.code()} : ${response.message()}"
                    errorMessageFlow.value = message
                    Log.d("ERROR", message)
                }
            }

            override fun onFailure(call: Call<Beer>, t: Throwable) {
                val message = t.message ?: "No connection to back-end"
                errorMessageFlow.value = message
                Log.d("APPLE", message)
            }
        })
    }

    fun sortBeersByBrewery(ascending: Boolean) {
        if(ascending) {
            beersFlow.value = beersFlow.value.sortedBy { it.brewery }
        } else {
            beersFlow.value = beersFlow.value.sortedByDescending { it.brewery }
        }
    }

    fun sortBeersByName(ascending: Boolean) {
        if(ascending) {
            beersFlow.value = beersFlow.value.sortedBy { it.name }
        } else {
            beersFlow.value = beersFlow.value.sortedByDescending { it.name }
        }
    }

    fun sortBeersByABV(ascending: Boolean) {
        if(ascending) {
            beersFlow.value = beersFlow.value.sortedBy { it.abv }
        } else {
            beersFlow.value = beersFlow.value.sortedByDescending { it.abv }
        }
    }

    fun sortBeersByVolume(ascending: Boolean) {
        if(ascending) {
            beersFlow.value = beersFlow.value.sortedBy { it.volume }
        } else {
            beersFlow.value = beersFlow.value.sortedByDescending { it.volume }
        }
    }

    fun filterByTitle(titleFragment: String) {
        if (titleFragment.isEmpty()) {
            getBeers()
            return
        }

        beersFlow.value = beersFlow.value.filter {
            it.name.contains(titleFragment, ignoreCase = true) ||
                    it.brewery.contains(titleFragment, ignoreCase = true)
        }
    }




}