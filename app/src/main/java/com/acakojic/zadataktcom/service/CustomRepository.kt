package com.acakojic.zadataktcom.service

import android.content.Context
import android.util.Log
import com.acakojic.zadataktcom.utility.EncryptedSharedPredManager
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

class CustomRepository(context: Context) {
    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://zadatak.tcom.rs/zadatak/public/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val apiServiceCoroutine: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://zadatak.tcom.rs/zadatak/public/api/")
            .addConverterFactory(LenientGsonConverterFactory())
            .build()
            .create(ApiService::class.java)
    }

    private val apiServiceWithoutBody: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://zadatak.tcom.rs/zadatak/public/api/")
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

    suspend fun addToFavorites(context: Context, vehicleID: Int): Result<Unit> {
            Log.i("CustomRepository", "POST /addToFavorites")
            val authToken = EncryptedSharedPredManager.getToken(context)
            try {
                val response = apiServiceWithoutBody.addToFavorites(authToken = "Bearer $authToken", vehicleID = vehicleID)
                if (response.isSuccessful) {
                    // If response is successful and there's no body, return success
                    return Result.success(Unit)
                } else {
                    // Handle the case where the response isn't successful
                    return Result.failure(RuntimeException("Response not successful"))
                }
            } catch (e: Exception) {
                // Handle any exceptions during the call
                return Result.failure(e)
            }
        }

}


class LenientGsonConverterFactory : Converter.Factory() {
    private val gson = GsonBuilder().create()

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        val adapter = gson.getAdapter(TypeToken.get(type))
        return Converter<ResponseBody, Any> { body ->
            if (body.contentLength() == 0L || body.contentLength() == -1L) {
                // Provide a default empty object for Gson to parse when there is no response body
                adapter.fromJson("{}")
            } else {
                adapter.fromJson(body.charStream())
            }
        }
    }
}


