package com.example.cs3130_bonusassignment.repository

import com.example.cs3130_bonusassignment.model.OverpassResponse
import com.example.cs3130_bonusassignment.network.OverpassService
import retrofit2.Call

class PoiRepository(private val api: OverpassService) {

    fun fetchCategory(category: String, bbox: String): Call<OverpassResponse> {
        val tagFilter = when (category) {
            "restaurant" -> """node["amenity"="restaurant"]($bbox);"""
            "fuel" -> """node["amenity"="fuel"]($bbox);"""
            "car_repair" -> """node["shop"="car_repair"]($bbox);"""
            "park" -> """node["leisure"="park"]($bbox);"""
            "supermarket" -> """node["shop"="supermarket"]($bbox);"""
            else -> ""
        }
        val ql = """
            [out:json];
            $tagFilter
            out;
        """.trimIndent()

        android.util.Log.d("OverpassQuery", ql)
        return api.getPOIS(ql)
    }
}