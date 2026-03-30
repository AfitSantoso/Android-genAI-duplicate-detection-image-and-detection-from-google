package com.example.detectionimgmobile.data.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val userId: String, private val role: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithHeaders = originalRequest.newBuilder()
            .header("X-User-Id", userId)
            .header("X-User-Role", role)
            .build()
        return chain.proceed(requestWithHeaders)
    }
}
