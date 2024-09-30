package com.example.beerbasement.repository

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.beerbasement.model.Beer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BeersRepository  {
    private val baseUrl = "https://anbo-restbeer.azurewebsites.net/api/"
    private val  beerBasementService  : BeerBasementService
    val beersFlow: MutableState<List<Beer>> = mutableStateOf(listOf())
     val isLoadingBeers = mutableStateOf(false)
    val errorMessageFlow = mutableStateOf("")

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        beerBasementService = retrofit.create(BeerBasementService::class.java)
        getBeers()
    }

    fun getBeers() {
        isLoadingBeers.value = true
        beerBasementService.GetAllBeers().enqueue(object : Callback<List<Beer>> {
            override fun onResponse(call: Call<List<Beer>>, response: Response<List<Beer>>) {
                if (response.isSuccessful) {
                    val beerList : List<Beer>? = response.body()
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
    }
