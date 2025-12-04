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

    private const val BASE_URL = "https://finnhub.io/api/v1/"

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

        // Finnhub API 인증 - 쿼리 파라미터로 token 추가
        val authInterceptor = okhttp3.Interceptor { chain ->
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url

            // URL에 token 쿼리 파라미터 추가
            val urlWithToken = originalUrl.newBuilder()
                .addQueryParameter("token", com.miyaong.invest.BuildConfig.FINNHUB_API_KEY)
                .build()

            val request = originalRequest.newBuilder()
                .url(urlWithToken)
                .build()

            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
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
}
