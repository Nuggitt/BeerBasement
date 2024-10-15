import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import com.example.beerbasement.model.Beer
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface BeerBasementService {
    @GET("beers")
    fun GetAllBeers(): Call<List<Beer>>

    @GET("beers/{id}") // This should match the placeholder in the URL
    fun getBeerById(@Path("id") beerId: Int): Call<Beer> // Corrected from beerId to id

    @GET("Beers/{username}")
    fun getBeersByUsername(@Path("username") username: String): Call<List<Beer>>

    @POST("beers")
    fun addBeer(@Body beer: Beer): Call<Beer>

    @DELETE("beers/{id}")
    fun deleteBeer(@Path("id") beerId: Int): Call<Beer>



}
