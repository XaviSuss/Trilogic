package com.example.trilogic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class WelcomeMessage(
    val message: String,
    val version: String
)

interface GameApiService {
    @GET("welcome.json")
    suspend fun getWelcomeMessage(): WelcomeMessage

    companion object {
        private const val BASE_URL = "https://raw.githubusercontent.com/Xavisuss/Trilogic/main/"

        fun create(): GameApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GameApiService::class.java)
        }
    }
}
