package com.acakojic.zadataktcom.service

import android.content.Context
import android.util.Log
import com.acakojic.zadataktcom.utility.EncryptedSharedPredManager
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CustomRepository(context: Context) {
    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://zadatak.tcom.rs/zadatak/public/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    suspend fun login(email: String): Response<LoginResponse> {
        Log.i("CustomRepository", "POST /login")
        return apiService.login(mapOf("email" to email))
    }

    suspend fun getAllVehicles(context: Context): Response<List<Vehicle>> {
        Log.i("CustomRepository", "GET /allVehicles")
        val authToken = EncryptedSharedPredManager.getToken(context)
        return apiService.getAllVehicles("Bearer $authToken")
    }
}
