package com.example.cepstun.helper

import com.example.cepstun.data.remote.retrofit.ApiConfigCurrency
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.withNumberingFormat(): String {
    return NumberFormat.getNumberInstance().format(this.toDouble())
}

fun String.withDateFormat(): String {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    val date = format.parse(this) as Date
    return DateFormat.getDateInstance(DateFormat.FULL).format(date)
}

suspend fun String.withCurrencyFormat(): String {
    val rupiahExchangeRate = getRupiahExchangeRate()
    var priceOnDollar = this.toDouble() / rupiahExchangeRate

    var mCurrencyFormat = NumberFormat.getCurrencyInstance()
    val deviceLocale = Locale.getDefault().country
    when {
        deviceLocale.equals("ID") -> {
            priceOnDollar *= rupiahExchangeRate
        }
        else -> {
            mCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        }
    }
    return mCurrencyFormat.format(priceOnDollar)
}

suspend fun getRupiahExchangeRate(): Float {
    val service = ApiConfigCurrency.getApiService()
    val response = service.getLatestCurrency()
    return response.usd?.idr ?: 0.0F
}

// use this to convert IDR
//lifecycleScope.launch {
//    val formattedCurrency = exampleStringPriceRupiah.withCurrencyFormat()
//    binding.TVPrice.text = formattedCurrency
//}