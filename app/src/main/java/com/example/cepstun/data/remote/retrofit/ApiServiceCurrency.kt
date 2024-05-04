package com.example.cepstun.data.remote.retrofit


import com.example.cepstun.data.remote.response.ResponseCurrencyLive
import retrofit2.http.GET

interface ApiServiceCurrency {
    @GET("v1/currencies/usd.json")
    suspend fun getLatestCurrency(): ResponseCurrencyLive
}