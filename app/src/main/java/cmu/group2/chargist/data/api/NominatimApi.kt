package cmu.group2.chargist.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

private const val BASE_URL = "https://nominatim.openstreetmap.org"

interface NominatimRest {
    @GET("search")
    @Headers("User-Agent: ChargIST location search")
    fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json"
    ): Call<List<GeoResponse>>
}

val NominatimApi: NominatimRest by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    retrofit.create(NominatimRest::class.java)
}

data class GeoResponse(
    @SerializedName("display_name") val displayName: String,
    @SerializedName("lat") val latitude: String,
    @SerializedName("lon") val longitude: String,
)
