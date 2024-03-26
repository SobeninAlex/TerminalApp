package com.example.terminal.data

import retrofit2.http.GET

interface ApiService {

    @GET("v2/aggs/ticker/AAPL/range/1/hour/2022-01-09/2023-01-09?adjusted=true&sort=asc&limit=50000&apiKey=BD59Lk8af58oEu63dbLeoecezASkApH5")
    suspend fun loadBars() : ResponseResult

}