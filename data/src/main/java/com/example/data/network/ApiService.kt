package com.example.data.network

import com.example.data.network.response.CatImageDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("v1/images/search")
    suspend fun getCatImages(
        @Query("limit") limit: Int = 20
    ): List<CatImageDto>
}