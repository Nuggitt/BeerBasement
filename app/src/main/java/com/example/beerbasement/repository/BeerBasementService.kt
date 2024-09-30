package com.example.beerbasement.repository
import retrofit2.Call
import retrofit2.http.*
import com.example.beerbasement.model.Beer


interface BeerBasementService {
    @GET("beers")
    fun GetAllBeers(): Call<List<Beer>>

}
