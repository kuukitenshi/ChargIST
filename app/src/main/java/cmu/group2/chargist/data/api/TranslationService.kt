package cmu.group2.chargist.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

private const val BASE_URL = "http://10.0.2.2:5000/"

interface TranslationRest {
    @POST("translate")
    fun translate(@Body req: TranslateRequest): Call<TranslateResponse>
}

val TranslationApi: TranslationRest by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    retrofit.create(TranslationRest::class.java)
}

data class TranslateRequest(
    @SerializedName("q") val text: String,
    val target: String,
    val source: String = "auto",
)

data class TranslateResponse(
    val translatedText: String,
)