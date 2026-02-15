package com.example.myapplication.data


import AddCookiesInterceptor
import ReceivedCookiesInterceptor
import android.content.Context
import com.example.marsphotos.data.NetworSNRepository
import com.example.marsphotos.data.SNRepository
import com.example.myapplication.network.SICENETWService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory


/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {

    val snRepository: SNRepository
    
}

/**
 * Implementation for the Dependency Injection container at the application level.
 *
 * Variables are initialized lazily and the same instance is shared across the whole app.
 */
class DefaultAppContainer(applicationContext: Context) : AppContainer {
    private val baseUrlSN = "https://sicenet.surguanajuato.tecnm.mx"
    private var client: OkHttpClient
    init {
        client = OkHttpClient()
        val builder = OkHttpClient.Builder()

        builder.addInterceptor(AddCookiesInterceptor(applicationContext)) // VERY VERY IMPORTANT

        builder.addInterceptor(ReceivedCookiesInterceptor(applicationContext)) // VERY VERY IMPORTANT

        client = builder.build()
    }

    private val retrofitSN: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrlSN)
        .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
        .client(client)
        .build()

    /**
     * Retrofit service object for creating api calls
     */
    private val retrofitServiceSN: SICENETWService by lazy {
        retrofitSN.create(SICENETWService::class.java)
    }
    /**
     * DI implementation for Mars photos repository
     */
    override val snRepository: NetworSNRepository by lazy {
        NetworSNRepository(retrofitServiceSN)
    }
}
