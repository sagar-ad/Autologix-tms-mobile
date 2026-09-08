# AutoLogix TMS - Android Companion Mobile Application

Enterprise Native Android application for **AutoLogix Trolley Management System (TMS)**.

## Architecture & Technology Stack

- **Platform:** Native Android (Kotlin)
- **UI Toolkit:** Jetpack Compose with Material 3 Design
- **Architecture Pattern:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Asynchronous & Streams:** Kotlin Coroutines + `StateFlow`
- **Networking:** Retrofit 2 + OkHttp 3 with dynamic URL switching & token interceptors
- **Navigation:** Navigation Compose (`NavHost`, `rememberNavController`)
- **State Handling:** `UiState` & `NetworkResult` (Loading, Success, Error, NetworkError, Empty)
- **Token Security:** `EncryptedSharedPreferences` (AES-256 GCM encryption via AndroidX Security Crypto)
- **Dependency Injection:** Container / Service Locator pattern (`AppContainer`, `DefaultAppContainer`)

## Project Structure

```
mobile-app_v1/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/autologix/tms/
│   │   │   ├── TmsApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── core/
│   │   │   │   ├── common/         # UiState, common utilities
│   │   │   │   ├── constants/      # AppConstants, ApiEndpoints
│   │   │   │   ├── di/             # AppContainer (Dependency Injection)
│   │   │   │   ├── navigation/     # Screen, AppNavGraph
│   │   │   │   ├── network/        # ApiConfig, RetrofitClient, AuthInterceptor, TokenAuthenticator, NetworkResult
│   │   │   │   ├── security/       # SecureTokenStorage, SessionManager
│   │   │   │   └── utils/          # NetworkUtils
│   │   │   ├── data/
│   │   │   │   ├── models/         # DTOs, ErrorResponseDto
│   │   │   │   ├── remote/         # AuthApiService (Retrofit interfaces)
│   │   │   │   └── repositories/   # AuthRepositoryImpl
│   │   │   ├── domain/
│   │   │   │   ├── models/         # AppEnvironment, UserSession
│   │   │   │   └── repositories/   # AuthRepository interface
│   │   │   ├── presentation/
│   │   │   │   ├── auth/           # SplashScreen, LoginScreen, LoginViewModel, LoginUiState
│   │   │   │   ├── dashboard/      # DashboardScreen, DashboardViewModel
│   │   │   │   ├── trolley/        # TrolleyScreen
│   │   │   │   ├── maintenance/    # MaintenanceScreen
│   │   │   │   ├── damage/         # DamageScreen
│   │   │   │   ├── logistics/      # LogisticsScreen
│   │   │   │   ├── reports/        # ReportsScreen
│   │   │   │   ├── notifications/  # NotificationsScreen
│   │   │   │   └── profile/        # ProfileScreen
│   │   │   └── ui/
│   │   │       ├── components/     # AppButton, AppTextField, AppTopBar, EmptyStateView, NetworkErrorView, LoadingIndicator
│   │   │       └── theme/          # Color, Type, Theme (AutoLogix Material 3)
│   │   └── res/
│   └── build.gradle.kts
├── gradle/
│   ├── wrapper/
│   └── libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

## Multi-Environment Configuration

The application includes dynamic runtime and build-time API environment configuration:

1. **Development (`DEVELOPMENT`):** Default endpoint targeting local/dev test server.
2. **UAT (`UAT`):** User Acceptance Testing environment.
3. **Production (`PRODUCTION`):** Secure enterprise production endpoint.

### Custom Endpoint Override
Operators and test engineers can configure or switch the endpoint at runtime directly from the Login screen or through `ApiConfig` without rebuilding or modifying business logic.

## Building and Generating the .APK File

### 1. Where to Get the Source Code
In Google AI Studio:
* Click the menu at the top right (Settings/Export) and select **Download as ZIP** or **Export to GitHub**.
* The complete Android project resides in the `/mobile-app_v1` directory.

### 2. Generate APK via Android Studio (Recommended)
1. Open Android Studio (Ladybug, Hedgehog, or newer).
2. Click **Open** and select the `mobile-app_v1` directory.
3. Wait for the Gradle project sync to complete.
4. In the top menu bar, select:
   **Build ➔ Build Bundle(s) / APK(s) ➔ Build APK(s)**
5. Once compilation finishes, a notification appears with a **locate** button. The generated APK is located at:
   ```
   mobile-app_v1/app/build/outputs/apk/debug/app-debug.apk
   ```

### 3. Generate APK via Command Line (Gradle)
If you have the Android SDK installed on your workstation:
```bash
cd mobile-app_v1
./gradlew assembleDebug
```
Output path:
```
mobile-app_v1/app/build/outputs/apk/debug/app-debug.apk
```

### 4. Automatic Cloud Build via GitHub Actions
When you export/push this repository to GitHub:
* A pre-configured workflow at `.github/workflows/build-apk.yml` automatically triggers on push or manual run (**Actions ➔ Build AutoLogix TMS Android APK ➔ Run workflow**).
* Once the action completes, download the **`autologix-tms-debug-apk`** ZIP directly from the GitHub Actions summary page, which contains the compiled `app-debug.apk`.
