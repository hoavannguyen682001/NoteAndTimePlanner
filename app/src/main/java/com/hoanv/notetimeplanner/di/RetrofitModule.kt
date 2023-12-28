package com.hoanv.notetimeplanner.di

import android.content.Context
import com.hoanv.notetimeplanner.data.remote.AppApi
import com.hoanv.notetimeplanner.utils.EnumConverterFactory
import com.hoanv.notetimeplanner.utils.NetworkConstant
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.hoanv.notetimeplanner.utils.interceptor.NetworkInterceptor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun providesConverterFactory(): Converter.Factory {
        val json = Json { ignoreUnknownKeys = true }
        val contentType = "application/json".toMediaType()
        return json.asConverterFactory(contentType)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        networkInterceptor: NetworkInterceptor,
    ): OkHttpClient {
        val myCache = Cache(context.cacheDir, NetworkConstant.CACHE_SIZE)

        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val builder = OkHttpClient.Builder()
            .cache(myCache)
            .addInterceptor(networkInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(NetworkConstant.TIME_OUT, TimeUnit.MILLISECONDS)
            .writeTimeout(NetworkConstant.TIME_OUT, TimeUnit.MILLISECONDS)
            .readTimeout(NetworkConstant.TIME_OUT, TimeUnit.MILLISECONDS)
        return builder.build()
    }

    @Singleton
    @Provides
    fun appApi(
        client: OkHttpClient,
        converterFactory: Converter.Factory
    ): AppApi {
        return Retrofit.Builder()
            .baseUrl(NetworkConstant.API_SERVER)
            .client(client)
            .addConverterFactory(converterFactory)
            .addConverterFactory(EnumConverterFactory())
            .build()
            .create(AppApi::class.java)
    }
}