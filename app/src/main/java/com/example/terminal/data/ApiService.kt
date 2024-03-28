package com.example.terminal.data

import com.example.terminal.presentation.TimeFrame
import com.example.terminal.utils.API_KEY
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("v2/aggs/ticker/{stocksTicker}/range/{multiplier}/{timespan}/{from}/{to}?sort=desc&limit=50000")
    suspend fun loadBars(
        @Path("stocksTicker") stocksTicker: String = "AAPL",
        @Path("multiplier") multiplier: String,
        @Path("timespan") timespan: String,
        @Path("from") from: String = "2022-01-09",
        @Path("to") to: String = "2023-01-09",
        @Query("adjusted") adjusted: Boolean = true,
        @Query("apiKey") apiKey: String = API_KEY,
    ) : ResponseResult

}