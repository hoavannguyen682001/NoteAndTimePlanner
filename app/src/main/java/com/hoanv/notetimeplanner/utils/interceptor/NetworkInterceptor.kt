package com.hoanv.notetimeplanner.utils.interceptor

import com.hoanv.notetimeplanner.utils.Pref
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

open class NetworkInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalHttpUrl = original.url

        val url = originalHttpUrl.newBuilder()
            .build()

        val requestBuilder = original.newBuilder()
            .addHeader("Authorization", "Bearer " + Pref.accessToken)
            .url(url)

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}