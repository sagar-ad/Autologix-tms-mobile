# AutoLogix TMS - Mobile Technical Architecture & Engineering Design

**Document Reference:** `docs/mobile-architecture.md`  
**System:** AutoLogix Trolley Management System (TMS)  
**Target Path:** `C:\repo\Autologix-TMS\mobile-app_v1`  
**Application:** Native Android Companion Application  
**Author:** Senior Android Solution Architect & Lead Developer  
**Status:** Architectural Specification (Awaiting Implementation Approval)

---

## 1. Architectural Principles & Overview

The AutoLogix TMS Mobile Companion application is designed from the ground up as a **high-reliability, offline-resilient, enterprise Android application**. It strictly obeys Android Clean Architecture and Unidirectional Data Flow (MVI/UDF) principles.

### Key Architectural Tenets:
1. **Zero Direct DB Access:** The Android app strictly consumes the existing NestJS REST APIs. No direct connection to PostgreSQL/MySQL or SQL Server exists.
2. **Business Rule Preservation:** State machine transitions (such as whether a trolley can be moved from `CUSTOMER` to `YARD`, or whether a PM checklist item passes) are validated by the backend; the mobile app presents the contracts accurately and handles offline queues safely.
3. **Hardware Agility:** Seamless integration with physical cameras, industrial laser barcode scanners (via Android intent wedge / BroadcastReceiver), and optical ML Kit scanning.
4. **Resilience & Battery Efficiency:** Network calls are offloaded to background Coroutine dispatchers (`Dispatchers.IO`), with lifecycle-aware execution in ViewModels (`viewModelScope`).

---

## 2. High-Level Clean Architecture Blueprint

```
+-----------------------------------------------------------------------------------+
|                              PRESENTATION LAYER                                   |
|  +-----------------------------------------------------------------------------+  |
|  | Jetpack Compose UI (Material 3)                                             |  |
|  | • Navigation Component (Single Activity Architecture)                       |  |
|  | • Screens: Login, Dashboard, Trolley360, GateInOut, PMChecklist, Damage     |  |
|  | • Components: BarcodeScannerView, ImageUploadPicker, OrgSelectorDropdown    |  |
|  +---------------------------------------^-------------------------------------+  |
|                                          | StateFlow<UiState> / Events            |
|  +---------------------------------------v-------------------------------------+  |
|  | ViewModels (Jetpack Architecture Components)                                |  |
|  | • MVI / Unidirectional Data Flow                                            |  |
|  | • AuthViewModel, TrolleyViewModel, GateInOutViewModel, PmViewModel         |  |
|  +---------------------------------------^-------------------------------------+  |
+------------------------------------------|----------------------------------------+
                                           | Coroutines Flow / Result<T>
+------------------------------------------v----------------------------------------+
|                                 DOMAIN LAYER                                      |
|  +-----------------------------------------------------------------------------+  |
|  | Use Cases / Interactors                                                     |  |
|  | • ScanTrolleyBarcodeUseCase        • ExecuteGateInOutUseCase                |  |
|  | • SubmitPmChecklistUseCase         • ReportDamageIncidentUseCase            |  |
|  | • RefreshAuthTokenUseCase          • SwitchActiveOrganizationUseCase        |  |
|  +---------------------------------------^-------------------------------------+  |
|  | Domain Models & Business Interfaces (Pure Kotlin, zero Android dependencies)|  |
|  +---------------------------------------|-------------------------------------+  |
+------------------------------------------|----------------------------------------+
                                           | Repository Interfaces
+------------------------------------------v----------------------------------------+
|                                  DATA LAYER                                       |
|  +-----------------------------------------------------------------------------+  |
|  | Repositories (Implementations)                                              |  |
|  | • TrolleyRepositoryImpl            • MovementRepositoryImpl                 |  |
|  | • MaintenanceRepositoryImpl        • AuthRepositoryImpl                     |  |
|  +-------------------+-----------------------------------+---------------------+  |
|                      |                                   |                        |
|  +-------------------v---------------+   +---------------v---------------------+  |
|  | Remote Data Source                |   | Local Data Source                   |  |
|  | • Retrofit 2 + Moshi / Kotlinx     |   | • Room Database (SQLCipher)         |  |
|  | • OkHttp 4 Client                 |   | • Offline Gate/PM Queue             |  |
|  | • AuthInterceptor (x-org-id + JWT) |   | • EncryptedSharedPreferences        |  |
|  | • TokenAuthenticator (Auto Refresh)|  | • Hardware Keystore MasterKey       |  |
|  +-----------------------------------+   +-------------------------------------+  |
+-----------------------------------------------------------------------------------+
```

---

## 3. Technology Stack & Library Matrix

| Component | Selected Technology | Purpose & Architectural Justification |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.0+ | Modern Android standard, type-safe, null-safe, coroutines native. |
| **UI Toolkit** | Jetpack Compose (BOM 2024+) | Declarative, dynamic Material 3 components, eliminates XML view bloat. |
| **Design System** | Material 3 (Material You) | Enterprise industrial aesthetic, high contrast, dark/light theme support. |
| **Dependency Injection**| Dagger Hilt 2.51+ | Standard Android DI, compile-time validation, deep ViewModel integration. |
| **Async & Concurrency** | Kotlin Coroutines & Flow | Asynchronous non-blocking network I/O, reactive UI state streams. |
| **Networking** | Retrofit 2 + OkHttp 4 | Industry standard HTTP client with interceptors, authenticator, logging. |
| **Serialization** | Kotlinx Serialization | Fast, reflection-free, robust JSON parsing for NestJS DTOs. |
| **Local Persistence** | Room 2.6+ with SQLCipher | Offline scanning cache and pending sync queues with 256-bit encryption. |
| **Secure Storage** | Jetpack Security (Crypto) | EncryptedSharedPreferences backed by hardware Android Keystore. |
| **Barcode/QR Scanning** | Google ML Kit Barcode Scanning | Fast on-device camera recognition (QR, Code 128, DataMatrix) via CameraX. |
| **Camera Integration** | CameraX (Core, Camera2, Lifecycle) | Reliable cross-device camera lifecycle management for industrial devices. |
| **Background Sync** | WorkManager | Guaranteed execution for offline gate scan queues and image uploads. |
| **Push Notifications** | Firebase Cloud Messaging (FCM) | Real-time work order assignment alerts and damage escalations. |
| **Image Loading** | Coil 2.6+ | Lightweight Kotlin-first image loader with disk/memory caching. |

---

## 4. Package Structure (`com.autologix.tms`)

```
com.autologix.tms/
│
├── core/
│   ├── common/             # Result wrapper, Constants, AppDispatchers
│   ├── network/            # Retrofit builder, AuthInterceptor, ErrorInterceptor
│   ├── database/           # Room Database, TypeConverters, Migration
│   ├── security/           # SecureTokenStorage, KeystoreManager, BiometricHelper
│   ├── designsystem/       # Compose Theme, Color, Typography, Shared Composables
│   └── util/               # BarcodeParser, ImageCompressor, DateFormatter
│
├── data/
│   ├── model/              # Remote DTOs (Request/Response) & Local Entities
│   ├── remote/             # Retrofit Service Interfaces (Auth, Trolley, PM, etc.)
│   ├── local/              # Room DAOs (TrolleyDao, OfflineSyncDao)
│   └── repository/         # Implementation of Domain Repository interfaces
│
├── domain/
│   ├── model/              # Pure domain models (Trolley, User, WorkOrder, etc.)
│   ├── repository/         # Repository interfaces (contracts)
│   └── usecase/            # Granular Use Cases (e.g. ScanTrolleyUseCase)
│
├── presentation/
│   ├── navigation/         # NavHost, AppDestinations, RoleBasedNavGraph
│   ├── common/             # StateLayouts, EmptyViews, ScanOverlays
│   ├── auth/               # LoginScreen, OrgSelectScreen, AuthViewModel
│   ├── dashboard/          # DashboardScreen, DashboardViewModel
│   ├── trolley/            # TrolleyListScreen, Trolley360Screen, TrolleyViewModel
│   ├── scanner/            # BarcodeScannerScreen, BatchScanScreen, ScannerViewModel
│   ├── logistics/          # GateInScreen, GateOutScreen, MovementViewModel
│   ├── maintenance/        # PmScheduleScreen, ChecklistScreen, WorkOrderViewModel
│   └── damage/             # ReportDamageScreen, DamageDetailScreen, DamageViewModel
│
├── service/
│   ├── sync/               # SyncWorker, SyncManager (WorkManager)
│   └── push/               # TmsFirebaseMessagingService
│
└── TmsApplication.kt       # Application class (@HiltAndroidApp)
```

---

## 5. Core Architectural Subsystems

### 5.1 Barcode & QR Code Scanning Engine (CameraX + ML Kit)
* **High-FPS Scanning:** Uses CameraX `ImageAnalysis.Builder` with `STRATEGY_KEEP_ONLY_LATEST` resolution 1280x720.
* **ML Kit Integration:** Multi-format barcode analyzer detecting:
  - `FORMAT_QR_CODE` (Standard AutoLogix 2D matrix on trolley tags)
  - `FORMAT_CODE_128` & `FORMAT_CODE_39` (Legacy linear barcodes)
  - `FORMAT_DATA_MATRIX` (Compact engraved metal plates on high-temp trolleys)
* **Haptic & Acoustic Feedback:** Instant vibration pulse and positive beep on successful decode.
* **Continuous Batch Mode:** Allows scanning 50 trolleys sequentially without closing the camera viewfinder, streaming scanned IDs into a local batch manifest.

### 5.2 Offline Synchronization Subsystem (WorkManager)
* **Challenge:** Dock doors and yard basements frequently lose connectivity.
* **Solution:**
  1. When an operator records a Gate IN/OUT or saves a PM checklist while offline, the repository saves the transaction to Room `offline_sync_queue` table with status `PENDING`.
  2. UI displays a non-intrusive badge: "3 transactions pending sync".
  3. `SyncWorker` is registered with WorkManager requiring `NetworkType.CONNECTED`.
  4. Once connectivity is restored, WorkManager executes atomic replay against NestJS APIs in strict chronological order.

### 5.3 Hardware Intent Wedge Support for Industrial Rugged Devices
* In addition to camera scanning, enterprise Android devices (Zebra TC57, Honeywell CT40) feature built-in hardware laser scan engines.
* The application registers a `BroadcastReceiver` listening for OEM intent wedges (`com.symbol.datawedge.api.ACTION_SOFTSCANTRIGGER`), directly piping the laser-decoded string into the active ViewModel!

---

## 6. Target Directory & Build Configuration

* **Project Root:** `C:\repo\Autologix-TMS\mobile-app_v1`
* **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalogs (`libs.versions.toml`).
* **Min SDK:** 26 (Android 8.0 - covers 99.2% of industrial enterprise scanners).
* **Target SDK:** 34 (Android 14).
* **Compile SDK:** 34.
