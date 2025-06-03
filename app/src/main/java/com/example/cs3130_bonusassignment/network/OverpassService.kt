package com.example.cs3130_bonusassignment.network

import com.example.cs3130_bonusassignment.model.OverpassResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassService {
    /*
    * @GET annotation to fetch the data from the endpoint of the API
    * @Param query is a string to be sent to the API endpoint
    */
    @GET("interpreter")
    fun getPOIS(
        @Query("data") query: String
    ): Call<OverpassResponse>
}