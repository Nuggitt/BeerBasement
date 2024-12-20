package com.example.beerbasement.repository

import BeerBasementService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.beerbasement.model.Beer
import com.google.firebase.auth.FirebaseAuth
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
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser?.email
    private var originalBeerList: List<Beer> = listOf() // Store the original list

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
                    Log.d("Get Beers virker", response.body().toString())
                    val beerList: List<Beer>? = response.body()
                    originalBeerList = beerList ?: emptyList() // Save the original list
                    beersFlow.value = originalBeerList // Set the flow to the original list
                    errorMessageFlow.value = ""
                } else {
                    val message = response.code().toString() + " : " + response.message()
                    errorMessageFlow.value = message
                    Log.d("Get beers fejler", message)
                }
            }

            override fun onFailure(call: Call<List<Beer>>, t: Throwable) {
                isLoadingBeers.value = false
                val message = t.message ?: "No connection to back-end"
                errorMessageFlow.value = message
                Log.d("Ingen forbindelse til rest server", message)
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
                    originalBeerList = beers ?: emptyList() // Update original beer list
                    beersFlow.value = originalBeerList // Set flow to the new list
                    errorMessageFlow.value = ""
                } else {
                    val message = "${response.code()} : ${response.message()}"
                    errorMessageFlow.value = message
                    Log.d("Fejl på getBeersByUserName", message)
                }
            }

            override fun onFailure(call: Call<List<Beer>>, t: Throwable) {
                isLoadingBeers.value = false
                val message = t.message ?: "No connection to back-end"
                errorMessageFlow.value = message
                Log.d("Ingen forbindelse til rest serveren", message)
            }
        })
    }

    fun addBeer(beer: Beer) {
        beerBasementService.addBeer(beer).enqueue(object : Callback<Beer> {
            override fun onResponse(call: Call<Beer>, response: Response<Beer>) {
                if (response.isSuccessful) {
                    Log.d("Add beer works", "Added beer" + response.body())
                    getBeers()
                } else {
                    val message = response.code().toString() + " : " + response.message()
                    errorMessageFlow.value = message
                    Log.d("Add beer fails", message)
                }
            }

            override fun onFailure(call: Call<Beer>, t: Throwable) {
                val message = t.message ?: "No connection to back-end"
                errorMessageFlow.value = message
                Log.d("Ingen mulighed for at adde til rest server", message)
            }
        })
    }

    fun deleteBeer(beerId: Int) {
        beerBasementService.deleteBeer(beerId).enqueue(object : Callback<Beer> {
            override fun onResponse(call: Call<Beer>, response: Response<Beer>) {
                if (response.isSuccessful) {
                    Log.d("Sletning fuldendt", "Deleted beer")
                    getBeers()
                } else {
                    val message = response.code().toString() + " : " + response.message()
                    errorMessageFlow.value = message
                    Log.d("sletning fejlet", message)
                }
            }

            override fun onFailure(call: Call<Beer>, t: Throwable) {
                val message = t.message ?: "No connection to back-end"
                errorMessageFlow.value = message
                Log.d("ingen forbindelse til rest server", message)
            }
        })
    }

    fun updateBeer(beerId: Int, beer: Beer) {
        // Use beerId for the update call
        beerBasementService.updateBeer(beerId, beer).enqueue(object : Callback<Beer> {
            override fun onResponse(call: Call<Beer>, response: Response<Beer>) {
                if (response.isSuccessful) {
                    Log.d("update succesfuld", "Updated beer")
                    getBeers() // Fetch updated list of beers
                } else {
                    val message = "${response.code()} : ${response.message()}"
                    errorMessageFlow.value = message
                    Log.d("uddate fejler", message)
                }
            }

            override fun onFailure(call: Call<Beer>, t: Throwable) {
                val message = t.message ?: "No connection to back-end"
                errorMessageFlow.value = message
                Log.d("Ingen fobindelse til rest api", message)
            }
        })
    }

    fun sortBeersByBrewery(ascending: Boolean) {
        if (ascending) {
            beersFlow.value = beersFlow.value.sortedBy { it.brewery }
        } else {
            beersFlow.value = beersFlow.value.sortedByDescending { it.brewery }
        }
    }

    fun sortBeersByName(ascending: Boolean) {
        if (ascending) {
            beersFlow.value = beersFlow.value.sortedBy { it.name }
        } else {
            beersFlow.value = beersFlow.value.sortedByDescending { it.name }
        }
    }

    fun sortBeersByABV(ascending: Boolean) {
        if (ascending) {
            beersFlow.value = beersFlow.value.sortedBy { it.abv }
        } else {
            beersFlow.value = beersFlow.value.sortedByDescending { it.abv }
        }
    }

    fun sortBeersByVolume(ascending: Boolean) {
        if (ascending) {
            beersFlow.value = beersFlow.value.sortedBy { it.volume }
        } else {
            beersFlow.value = beersFlow.value.sortedByDescending { it.volume }
        }
    }

    fun filterByTitle(titleFragment: String): List<Beer> {
        if (titleFragment.isEmpty()) {
            getBeersByUsername(currentUser ?: "")
            return beersFlow.value
        }


        val filteredBeers = originalBeerList.filter {
            it.name.contains(titleFragment, ignoreCase = true) ||
                    it.brewery.contains(titleFragment, ignoreCase = true)
        }

        // Update the beersFlow with the new filtered list
        beersFlow.value = filteredBeers

        // Return the filtered list
        return filteredBeers
    }

    fun NavigateToUrlSite(context: Context, url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }


}
