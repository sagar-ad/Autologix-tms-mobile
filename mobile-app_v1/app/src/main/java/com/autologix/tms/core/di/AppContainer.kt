package com.autologix.tms.core.di

import android.content.Context
import com.autologix.tms.core.network.ApiConfig
import com.autologix.tms.core.network.RetrofitClient
import com.autologix.tms.core.security.SecureTokenStorage
import com.autologix.tms.core.security.SessionManager
import com.autologix.tms.data.remote.AdminApiService
import com.autologix.tms.data.remote.AuthApiService
import com.autologix.tms.data.remote.CustomerApiService
import com.autologix.tms.data.remote.LogisticsApiService
import com.autologix.tms.data.remote.MaintenanceApiService
import com.autologix.tms.data.repositories.AdminRepositoryImpl
import com.autologix.tms.data.repositories.AuthRepositoryImpl
import com.autologix.tms.data.repositories.CustomerRepositoryImpl
import com.autologix.tms.data.repositories.LogisticsRepositoryImpl
import com.autologix.tms.data.repositories.MaintenanceRepositoryImpl
import com.autologix.tms.domain.repositories.AdminRepository
import com.autologix.tms.domain.repositories.AuthRepository
import com.autologix.tms.domain.repositories.CustomerRepository
import com.autologix.tms.domain.repositories.LogisticsRepository
import com.autologix.tms.domain.repositories.MaintenanceRepository

interface AppContainer {
    val tokenStorage: SecureTokenStorage
    val apiConfig: ApiConfig
    val sessionManager: SessionManager
    val retrofitClient: RetrofitClient
    val authApiService: AuthApiService
    val authRepository: AuthRepository
    val customerApiService: CustomerApiService
    val customerRepository: CustomerRepository
    val maintenanceApiService: MaintenanceApiService
    val maintenanceRepository: MaintenanceRepository
    val logisticsApiService: LogisticsApiService
    val logisticsRepository: LogisticsRepository
    val adminApiService: AdminApiService
    val adminRepository: AdminRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val tokenStorage: SecureTokenStorage by lazy {
        SecureTokenStorage(context)
    }

    override val apiConfig: ApiConfig by lazy {
        ApiConfig.getInstance()
    }

    override val sessionManager: SessionManager by lazy {
        SessionManager(tokenStorage, apiConfig)
    }

    override val retrofitClient: RetrofitClient by lazy {
        RetrofitClient(apiConfig, tokenStorage)
    }

    override val authApiService: AuthApiService by lazy {
        retrofitClient.retrofit.create(AuthApiService::class.java)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            authApiService = authApiService,
            sessionManager = sessionManager
        )
    }

    override val customerApiService: CustomerApiService by lazy {
        retrofitClient.retrofit.create(CustomerApiService::class.java)
    }

    override val customerRepository: CustomerRepository by lazy {
        CustomerRepositoryImpl(
            customerApiService = customerApiService
        )
    }

    override val maintenanceApiService: MaintenanceApiService by lazy {
        retrofitClient.retrofit.create(MaintenanceApiService::class.java)
    }

    override val maintenanceRepository: MaintenanceRepository by lazy {
        MaintenanceRepositoryImpl(
            apiService = maintenanceApiService
        )
    }

    override val logisticsApiService: LogisticsApiService by lazy {
        retrofitClient.retrofit.create(LogisticsApiService::class.java)
    }

    override val logisticsRepository: LogisticsRepository by lazy {
        LogisticsRepositoryImpl(
            apiService = logisticsApiService
        )
    }

    override val adminApiService: AdminApiService by lazy {
        retrofitClient.retrofit.create(AdminApiService::class.java)
    }

    override val adminRepository: AdminRepository by lazy {
        AdminRepositoryImpl(
            apiService = adminApiService
        )
    }
}
