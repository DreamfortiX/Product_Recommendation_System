
package com.example.recommendation_system.data.api


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // IMPORTANT: Change this to your computer's IP address
    // On Windows: ipconfig in Command Prompt
    // On Mac/Linux: ifconfig or ip addr
    // Use your local IP, not 127.0.0.1 or localhost
    private const val BASE_URL = "http://10.227.193.244:8000/"  // Your computer's IP

    // For testing with emulator, you can use 10.0.2.2 for localhost
    // private const val BASE_URL = "http://10.0.2.2:8000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)  // Increased to 60 seconds
        .readTimeout(60, TimeUnit.SECONDS)     // Increased to 60 seconds
        .writeTimeout(60, TimeUnit.SECONDS)    // Increased to 60 seconds
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    // Helper method to test connection
    fun getBaseUrl(): String = BASE_URL
}