import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import com.example.beerbasement.model.Beer
import okhttp3.RequestBody
import retrofit2.http.Headers


interface BeerBasementService {
    @GET("beers")
    fun GetAllBeers(): Call<List<Beer>>

    @GET("Beers/{username}")
    fun getBeersByUsername(@Path("username") username: String): Call<List<Beer>>

    @POST("beers")
    fun addBeer(@Body beer: Beer): Call<Beer>

    @DELETE("beers/{id}")
    fun deleteBeer(@Path("id") beerId: Int): Call<Beer>

    @PUT("beers/{id}")
    fun updateBeer(@Path("id") beerId: Int, @Body beer: Beer): Call<Beer>





}
