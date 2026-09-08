import React, { useState } from 'react';
import {
  FolderTree,
  FileCode,
  Shield,
  Layers,
  Network,
  Cpu,
  CheckCircle2,
  Copy,
  ExternalLink,
  Smartphone,
  Server,
  RefreshCw,
  LogOut,
  Sliders,
  AlertTriangle
} from 'lucide-react';

interface CodeSnippet {
  path: string;
  category: string;
  description: string;
  code: string;
}

const KOTLIN_SNIPPETS: CodeSnippet[] = [
  {
    path: 'core/network/ApiConfig.kt',
    category: 'Dynamic Environment Layer',
    description: 'Dynamic URL resolution without touching business logic, supporting Dev, UAT, and Production.',
    code: `package com.autologix.tms.core.network

import com.autologix.tms.core.security.SessionManager
import com.autologix.tms.domain.models.AppEnvironment
import kotlinx.coroutines.flow.StateFlow
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class ApiConfig(
    private val sessionManager: SessionManager
) {
    val currentEnvironment: StateFlow<AppEnvironment> = sessionManager.selectedEnvironment
    val customBaseUrl: StateFlow<String?> = sessionManager.customBaseUrl

    fun getBaseUrl(): String {
        val custom = customBaseUrl.value
        if (!custom.isNullOrBlank() && isValidUrl(custom)) {
            return if (custom.endsWith("/")) custom else "$custom/"
        }
        val env = currentEnvironment.value
        return if (env.defaultBaseUrl.endsWith("/")) env.defaultBaseUrl else "\${env.defaultBaseUrl}/"
    }

    fun isValidUrl(url: String): Boolean {
        val httpUrl: HttpUrl? = url.toHttpUrlOrNull()
        return httpUrl != null && (httpUrl.scheme == "http" || httpUrl.scheme == "https")
    }
}`
  },
  {
    path: 'core/security/SecureTokenStorage.kt',
    category: 'Encrypted Token Storage',
    description: 'Hardware-backed AES-256 GCM encrypted storage via AndroidX Security Crypto.',
    code: `package com.autologix.tms.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureTokenStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "autologix_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveTokens(accessToken: String, refreshToken: String?) {
        sharedPreferences.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}`
  },
  {
    path: 'core/network/RetrofitClient.kt',
    category: 'Networking & Interceptors',
    description: 'Dynamic OkHttp client with dynamic Host resolution, Authorization Interceptor, and TokenAuthenticator.',
    code: `package com.autologix.tms.core.network

import com.autologix.tms.core.security.SessionManager
import com.autologix.tms.data.remote.AuthApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    fun createOkHttpClient(
        apiConfig: ApiConfig,
        sessionManager: SessionManager
    ): OkHttpClient {
        val dynamicHostInterceptor = DynamicHostInterceptor(apiConfig)
        val authInterceptor = AuthInterceptor(sessionManager)

        return OkHttpClient.Builder()
            .addInterceptor(dynamicHostInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}`
  },
  {
    path: 'presentation/auth/LoginViewModel.kt',
    category: 'MVVM & StateFlow',
    description: 'Reactive login state machine supporting runtime environment switching, validation, loading & error states.',
    code: `class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val apiConfig: ApiConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login() {
        val currentState = _uiState.value
        if (!currentState.isSubmitEnabled) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null, isNetworkError = false) }

        viewModelScope.launch {
            val result = authRepository.login(
                email = currentState.email,
                password = currentState.password,
                organizationId = currentState.organizationId.ifBlank { null }
            )

            when (result) {
                is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                is NetworkResult.NetworkError -> _uiState.update { it.copy(isLoading = false, isNetworkError = true) }
            }
        }
    }
}`
  },
  {
    path: 'core/di/AppContainer.kt',
    category: 'Dependency Injection',
    description: 'Clean container pattern providing modular lazy singletons without heavy annotation processors.',
    code: `interface AppContainer {
    val tokenStorage: SecureTokenStorage
    val sessionManager: SessionManager
    val apiConfig: ApiConfig
    val authApiService: AuthApiService
    val authRepository: AuthRepository
    val customerApiService: CustomerApiService
    val customerRepository: CustomerRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val tokenStorage by lazy { SecureTokenStorage(context) }
    override val sessionManager by lazy { SessionManager(tokenStorage) }
    override val apiConfig by lazy { ApiConfig(sessionManager) }
    override val authApiService by lazy { RetrofitClient.createAuthApiService(apiConfig, sessionManager) }
    override val authRepository by lazy { AuthRepositoryImpl(authApiService, sessionManager) }
    override val customerApiService by lazy { RetrofitClient.createCustomerApiService(apiConfig, sessionManager) }
    override val customerRepository by lazy { CustomerRepositoryImpl(customerApiService, sessionManager) }
}`
  },
  {
    path: 'data/remote/CustomerApiService.kt',
    category: 'Customer API Mapping',
    description: 'Direct Retrofit interface mapping to existing NestJS endpoints for Dashboard, Trolleys, 360 View, Damages, Media Upload, Notifications & Reports.',
    code: `package com.autologix.tms.data.remote

import com.autologix.tms.data.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface CustomerApiService {
    @GET(ApiEndpoints.Customer.DASHBOARD_KPIS)
    suspend fun getCustomerDashboardKpis(): Response<CustomerDashboardKpiDto>

    @GET(ApiEndpoints.Customer.TROLLEYS)
    suspend fun getTrolleys(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<TrolleySummaryDto>>

    @GET(ApiEndpoints.Customer.TROLLEY_SCAN)
    suspend fun scanTrolley(@Path("barcode") barcode: String): Response<TrolleyQuickScanDto>

    @GET(ApiEndpoints.Customer.TROLLEY_DETAIL)
    suspend fun getTrolleyDetail(@Path("id") id: String): Response<TrolleyFullDetailDto>

    @GET(ApiEndpoints.Customer.TROLLEY_360)
    suspend fun getTrolley360(@Path("id") id: String): Response<Trolley360ViewDto>

    @Multipart
    @POST(ApiEndpoints.Customer.MEDIA_UPLOAD)
    suspend fun uploadMedia(@Part file: MultipartBody.Part): Response<MediaUploadResponseDto>

    @POST(ApiEndpoints.Customer.DAMAGE_REPORTS)
    suspend fun submitDamageRequest(@Body request: ReportDamageRequestDto): Response<DamageReportDto>

    @GET(ApiEndpoints.Customer.DAMAGE_REPORTS)
    suspend fun getDamageRequests(@Query("status") status: String? = null): Response<List<DamageReportDto>>
}`
  },
  {
    path: 'presentation/customer/RaiseDamageViewModel.kt',
    category: 'Customer Damage Request Flow',
    description: 'State machine for checklists, concern descriptions, photo uploads via backend media API, and pre-submit confirmation.',
    code: `class RaiseDamageViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RaiseDamageUiState())
    val uiState: StateFlow<RaiseDamageUiState> = _uiState.asStateFlow()

    fun confirmAndSubmit() {
        val currentState = _uiState.value
        viewModelScope.launch {
            val requestDto = ReportDamageRequestDto(
                trolleyId = currentState.trolleyId,
                severity = currentState.severity,
                category = currentState.category,
                description = currentState.damageDescription.trim(),
                concernDetails = currentState.concernDetails.ifBlank { null },
                selectedChecklistItems = currentState.selectedChecklistIds.toList(),
                photoUrls = currentState.photos.mapNotNull { it.remoteUrl }
            )
            val result = customerRepository.submitDamageRequest(requestDto)
            if (result is NetworkResult.Success) {
                _uiState.update { it.copy(isSubmitting = false, submittedReport = result.data) }
            }
        }
    }
}`
  }
];

export const AndroidProjectView: React.FC = () => {
  const [selectedSnippetIndex, setSelectedSnippetIndex] = useState(0);
  const [copied, setCopied] = useState(false);

  const selectedSnippet = KOTLIN_SNIPPETS[selectedSnippetIndex];

  const handleCopy = () => {
    navigator.clipboard.writeText(selectedSnippet.code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="space-y-6">
      {/* Foundation Status Banner */}
      <div className="bg-gradient-to-r from-emerald-950/40 via-slate-900 to-slate-950 border border-emerald-500/30 rounded-2xl p-6 shadow-xl">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <span className="flex h-3 w-3 rounded-full bg-emerald-400 animate-pulse"></span>
              <span className="text-xs font-bold uppercase tracking-wider text-emerald-400">
                Native Android Foundation Active
              </span>
              <span className="text-xs px-2 py-0.5 rounded-full bg-blue-500/20 text-blue-300 font-mono">
                com.autologix.tms
              </span>
            </div>
            <h2 className="text-xl font-bold text-white tracking-tight">
              AutoLogix TMS — Android Mobile Application (mobile-app_v1)
            </h2>
            <p className="text-xs text-slate-300 max-w-3xl leading-relaxed">
              Clean architecture Android companion project initialized with zero hardcoded mock business data,
              zero fake backend APIs, dynamic multi-environment configuration (Development, UAT, Production),
              AES-256 hardware-backed secure token storage, and Jetpack Compose Material 3 UI foundation.
            </p>
          </div>

          <div className="flex flex-wrap gap-2 shrink-0">
            <div className="bg-slate-800/80 border border-slate-700/80 px-3 py-2 rounded-xl text-center">
              <span className="text-[10px] uppercase text-slate-400 block font-semibold">Language</span>
              <span className="text-xs font-bold text-emerald-400 font-mono">Kotlin 2.0.21</span>
            </div>
            <div className="bg-slate-800/80 border border-slate-700/80 px-3 py-2 rounded-xl text-center">
              <span className="text-[10px] uppercase text-slate-400 block font-semibold">UI Toolkit</span>
              <span className="text-xs font-bold text-blue-400 font-mono">Compose BOM 2024.09</span>
            </div>
            <div className="bg-slate-800/80 border border-slate-700/80 px-3 py-2 rounded-xl text-center">
              <span className="text-[10px] uppercase text-slate-400 block font-semibold">Min / Target SDK</span>
              <span className="text-xs font-bold text-purple-400 font-mono">26 / 34</span>
            </div>
          </div>
        </div>
      </div>

      {/* Architecture Requirements Checklist */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 space-y-2">
          <div className="flex items-center gap-2 text-blue-400">
            <Cpu className="w-4 h-4" />
            <h3 className="text-xs font-bold uppercase tracking-wider">MVVM & Coroutines</h3>
          </div>
          <p className="text-xs text-slate-400">
            Jetpack ViewModels paired with Kotlin Coroutines and immutable StateFlow state machine.
          </p>
          <div className="flex items-center gap-1.5 text-[11px] text-emerald-400 font-medium">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Login & Dashboard ViewModels</span>
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 space-y-2">
          <div className="flex items-center gap-2 text-emerald-400">
            <Sliders className="w-4 h-4" />
            <h3 className="text-xs font-bold uppercase tracking-wider">Dynamic Environments</h3>
          </div>
          <p className="text-xs text-slate-400">
            ApiConfig layer switches baseUrl at runtime between Dev, UAT, Prod without business logic changes.
          </p>
          <div className="flex items-center gap-1.5 text-[11px] text-emerald-400 font-medium">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>DynamicHostInterceptor Active</span>
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 space-y-2">
          <div className="flex items-center gap-2 text-amber-400">
            <Shield className="w-4 h-4" />
            <h3 className="text-xs font-bold uppercase tracking-wider">Secure Token Storage</h3>
          </div>
          <p className="text-xs text-slate-400">
            AES-256 GCM encrypted shared preferences storing JWT access and refresh tokens.
          </p>
          <div className="flex items-center gap-1.5 text-[11px] text-emerald-400 font-medium">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>EncryptedSharedPreferences</span>
          </div>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 space-y-2">
          <div className="flex items-center gap-2 text-purple-400">
            <Layers className="w-4 h-4" />
            <h3 className="text-xs font-bold uppercase tracking-wider">Scalable Modular Core</h3>
          </div>
          <p className="text-xs text-slate-400">
            app/, core/, data/, domain/, presentation/ (trolley, maintenance, damage, logistics, reports).
          </p>
          <div className="flex items-center gap-1.5 text-[11px] text-emerald-400 font-medium">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Repository & AppContainer</span>
          </div>
        </div>
      </div>

      {/* Directory Tree & Code Viewer Split */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Module Structure Explorer */}
        <div className="lg:col-span-4 bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <FolderTree className="w-4 h-4 text-blue-400" />
              <h3 className="text-sm font-bold text-white">Project File Structure</h3>
            </div>
            <span className="text-[10px] font-mono text-slate-400 bg-slate-800 px-2 py-0.5 rounded">
              mobile-app_v1
            </span>
          </div>

          <div className="space-y-1.5 text-xs font-mono">
            <div className="text-slate-400 font-bold">mobile-app_v1/</div>
            <div className="pl-3 border-l border-slate-800 space-y-1">
              <div className="text-blue-400 font-semibold">├── app/</div>
              <div className="pl-4 border-l border-slate-800 space-y-1">
                <div>├── AndroidManifest.xml</div>
                <div>├── build.gradle.kts</div>
                <div className="text-emerald-400 font-semibold">└── src/main/java/com/autologix/tms/</div>
                <div className="pl-4 border-l border-slate-800 space-y-1 text-slate-300">
                  <div>├── TmsApplication.kt</div>
                  <div>├── MainActivity.kt</div>
                  <div className="text-amber-400">├── core/</div>
                  <div className="pl-4 text-slate-400 space-y-0.5">
                    <div>├── common/ (UiState.kt)</div>
                    <div>├── constants/ (AppConstants.kt, ApiEndpoints.kt)</div>
                    <div>├── di/ (AppContainer.kt)</div>
                    <div>├── navigation/ (Screen.kt, NavGraph.kt)</div>
                    <div>├── network/ (ApiConfig.kt, RetrofitClient.kt, Interceptors)</div>
                    <div>└── security/ (SecureTokenStorage.kt, SessionManager.kt)</div>
                  </div>
                  <div className="text-purple-400">├── data/</div>
                  <div className="pl-4 text-slate-400 space-y-0.5">
                    <div>├── models/ (AuthDtos.kt, ErrorResponseDto.kt)</div>
                    <div>├── remote/ (AuthApiService.kt)</div>
                    <div>└── repositories/ (AuthRepositoryImpl.kt)</div>
                  </div>
                  <div className="text-sky-400">├── domain/</div>
                  <div className="pl-4 text-slate-400 space-y-0.5">
                    <div>├── models/ (AppEnvironment.kt, UserSession.kt)</div>
                    <div>└── repositories/ (AuthRepository.kt)</div>
                  </div>
                  <div className="text-rose-400">├── presentation/</div>
                  <div className="pl-4 text-slate-400 space-y-0.5">
                    <div>├── auth/ (LoginScreen, LoginViewModel, Splash)</div>
                    <div>├── dashboard/ (DashboardScreen, ViewModel)</div>
                    <div>├── trolley/ (TrolleyScreen)</div>
                    <div>├── maintenance/ (MaintenanceScreen)</div>
                    <div>├── damage/ (DamageScreen)</div>
                    <div>├── logistics/ (LogisticsScreen)</div>
                    <div>├── reports/ (ReportsScreen)</div>
                    <div>├── notifications/ (NotificationsScreen)</div>
                    <div>└── profile/ (ProfileScreen)</div>
                  </div>
                  <div className="text-teal-400">└── ui/</div>
                  <div className="pl-4 text-slate-400 space-y-0.5">
                    <div>├── components/ (AppButton, AppTextField, EmptyStateView, NetworkErrorView)</div>
                    <div>└── theme/ (Color.kt, Theme.kt, Type.kt)</div>
                  </div>
                </div>
              </div>
              <div className="text-slate-400 font-semibold">├── gradle/libs.versions.toml</div>
              <div className="text-slate-400 font-semibold">├── gradlew & gradlew.bat</div>
              <div className="text-slate-400 font-semibold">└── build.gradle.kts</div>
            </div>
          </div>
        </div>

        {/* Code Snippet Inspector */}
        <div className="lg:col-span-8 bg-slate-900 border border-slate-800 rounded-2xl flex flex-col overflow-hidden">
          <div className="p-4 border-b border-slate-800 bg-slate-950/60 flex flex-wrap items-center justify-between gap-3">
            <div className="flex flex-wrap gap-1.5">
              {KOTLIN_SNIPPETS.map((snippet, idx) => (
                <button
                  key={snippet.path}
                  onClick={() => setSelectedSnippetIndex(idx)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
                    selectedSnippetIndex === idx
                      ? 'bg-blue-600 text-white shadow-sm'
                      : 'bg-slate-800 text-slate-400 hover:text-slate-200'
                  }`}
                >
                  {snippet.path.split('/').pop()}
                </button>
              ))}
            </div>

            <button
              onClick={handleCopy}
              className="flex items-center gap-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 px-3 py-1.5 rounded-lg text-xs font-medium border border-slate-700 transition-all"
            >
              <Copy className="w-3.5 h-3.5" />
              {copied ? 'Copied' : 'Copy Code'}
            </button>
          </div>

          <div className="px-5 py-3 bg-slate-900/90 border-b border-slate-800/80 flex items-center justify-between text-xs">
            <div>
              <span className="text-slate-400">File: </span>
              <span className="font-mono text-blue-300 font-semibold">
                mobile-app_v1/app/src/main/java/com/autologix/tms/{selectedSnippet.path}
              </span>
            </div>
            <span className="px-2 py-0.5 bg-blue-500/10 text-blue-400 rounded-full font-medium text-[11px]">
              {selectedSnippet.category}
            </span>
          </div>

          <div className="p-4 bg-slate-950 flex-1 overflow-x-auto font-mono text-xs text-slate-200 leading-relaxed">
            <pre>
              <code>{selectedSnippet.code}</code>
            </pre>
          </div>

          <div className="p-4 bg-slate-900/60 border-t border-slate-800 text-xs text-slate-400 flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>{selectedSnippet.description}</span>
          </div>
        </div>
      </div>
    </div>
  );
};
