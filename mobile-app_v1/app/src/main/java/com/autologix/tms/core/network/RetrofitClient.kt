package com.autologix.tms.core.network

import com.autologix.tms.core.security.SecureTokenStorage
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class DynamicHostInterceptor(private val apiConfig: ApiConfig) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val currentBaseUrl = apiConfig.getBaseUrl()
        val newHttpUrl = currentBaseUrl.toHttpUrlOrNull()

        if (newHttpUrl != null) {
            val originalUrl = request.url
            val newUrlBuilder = originalUrl.newBuilder()
                .scheme(newHttpUrl.scheme)
                .host(newHttpUrl.host)
                .port(newHttpUrl.port)

            // Reconstruct path segments ensuring base prefix is respected
            val baseSegments = newHttpUrl.pathSegments.filter { it.isNotEmpty() }
            val originalSegments = originalUrl.pathSegments.filter { it.isNotEmpty() }

            val combinedSegments = (baseSegments + originalSegments).distinct()
            newUrlBuilder.encodedPath("/")
            combinedSegments.forEach { segment ->
                newUrlBuilder.addPathSegment(segment)
            }

            request = request.newBuilder()
                .url(newUrlBuilder.build())
                .build()
        }

        return chain.proceed(request)
    }
}

class AuthInterceptor(private val tokenStorage: SecureTokenStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenStorage.getAccessToken()

        val requestBuilder = original.newBuilder()
            .header("Accept", "application/json")
            .header("X-Client-Platform", "Android-Companion")

        if (!token.isNullOrBlank() && original.header("Authorization") == null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}

class RetrofitClient(
    private val apiConfig: ApiConfig,
    private val tokenStorage: SecureTokenStorage
) {
    val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(DynamicHostInterceptor(apiConfig))
            .addInterceptor(AuthInterceptor(tokenStorage))
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(apiConfig.getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
