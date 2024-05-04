package com.example.cepstun.data.remote.response

import com.google.gson.annotations.SerializedName

data class ResponseCurrencyLive(
	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("usd")
	val usd: Usd? = null
)

data class Usd(

	@field:SerializedName("idr")
	val idr: Float
)