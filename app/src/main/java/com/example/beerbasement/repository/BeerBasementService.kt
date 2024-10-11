import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import com.example.beerbasement.model.Beer

interface BeerBasementService {
    @GET("beers")
    fun GetAllBeers(): Call<List<Beer>>

    @GET("beers/{id}") // This should match the placeholder in the URL
    fun getBeerById(@Path("id") beerId: Int): Call<Beer> // Corrected from beerId to id

    @GET("Beers/{username}")
    fun getBeersByUsername(@Path("username") username: String): Call<List<Beer>>

}
