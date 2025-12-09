package com.miyaong.invest.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.miyaong.invest.data.api.YahooFinanceApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // For Android Emulator: 10.0.2.2 points to host machine's localhost
    // For physical device: use your computer's IP address (e.g., 192.168.x.x)
    private const val BASE_URL = "https://invest-qviy.onrender.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideYahooFinanceApi(retrofit: Retrofit): YahooFinanceApi {
        return retrofit.create(YahooFinanceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFinnhubApi(retrofit: Retrofit): com.miyaong.invest.data.api.FinnhubApi {
        return retrofit.create(com.miyaong.invest.data.api.FinnhubApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStockApiService(retrofit: Retrofit): com.miyaong.invest.data.api.StockApiService {
        return retrofit.create(com.miyaong.invest.data.api.StockApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMacroApiService(retrofit: Retrofit): com.miyaong.invest.data.api.MacroApiService {
        return retrofit.create(com.miyaong.invest.data.api.MacroApiService::class.java)
    }
}
