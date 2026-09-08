export interface DocItem {
  id: string;
  name: string;
  path: string;
  description: string;
  content: string;
}

export const DOCS_LIST: DocItem[] = [
  {
    id: 'api-inventory',
    name: 'mobile-api-inventory.md',
    path: 'docs/mobile-api-inventory.md',
    description: '32 API categories, 47 endpoints, request/response models, and mobile suitability matrix.',
    content: `# AutoLogix TMS - Mobile REST API Inventory & Reuse Assessment

**Document Reference:** docs/mobile-api-inventory.md  
**System:** AutoLogix Trolley Management System (TMS)  
**Target Client:** Native Android Mobile Application (mobile-app_v1)  
**Backend:** NestJS REST APIs (PostgreSQL / MySQL Server / Multi-tenant Engine)  
**Author:** Senior Android Solution Architect & Lead Developer  
**Status:** Review & Architectural Baseline (Awaiting Implementation Approval)

---

## 1. Executive Summary & Inventory Scope

This inventory comprehensively documents **32 functional API categories** across the existing AutoLogix TMS NestJS backend. Each endpoint has been evaluated against mobile field operating realities:
* **Connectivity constraints:** Unstable Wi-Fi/LTE in manufacturing yards and logistics docks.
* **Payload footprint:** Avoiding multi-megabyte over-fetching common in Angular desktop views.
* **Hardware coupling:** Rapid barcode/QR decoding, CameraX capture, and push notification triggers.
* **Multi-tenancy:** Strict isolation via organizational token claims and x-organization-id boundary enforcement.

---

## 2. API Classification & Legend

| Field | Description |
| :--- | :--- |
| **HTTP Method** | GET, POST, PUT, PATCH, DELETE |
| **Endpoint** | Absolute REST path relative to /api/v1 base route |
| **Auth** | Public (No token), Bearer (JWT Access Token), Refresh (Refresh Token) |
| **RBAC Roles** | SA (Super Admin), OA (Org Admin), MM (Maintenance Manager), MT (Maintenance Tech), LO (Logistics Operator), VI (Viewer/Customer) |
| **Org Req** | Header (x-organization-id), JWT (Embedded in claims), None |
| **Pagination** | Yes (standard page, limit, sortBy, sortDir), Cursor (timestamp-based), No |
| **Mobile Suitability** | Direct (Ready as-is), Optimizable (Usable, but needs payload trim or DTO), Restricted (Desktop only) |
| **Action** | Reuse Direct, Extend/Wrap, Do Not Expose |

---

## 3. Comprehensive 32-Category API Matrix

### 3.1 Authentication & Session Management (Categories 1 - 4)
- POST /api/v1/auth/login [Public, All] -> AuthResponseDto (Reuse Direct)
- POST /api/v1/auth/refresh [Refresh, All] -> TokenPairDto (Reuse Direct)
- POST /api/v1/auth/logout [Bearer, All] -> MessageResponseDto (Reuse Direct)
- POST /api/v1/auth/forgot-password [Public, All] -> MessageResponseDto (Reuse Direct)
- POST /api/v1/auth/reset-password [Public, All] -> MessageResponseDto (Reuse Direct)

### 3.2 User Profile, Organization & Roles (Categories 5 - 7)
- GET /api/v1/users/me [Bearer, All, Org: JWT] -> UserProfileDto (Reuse Direct)
- PATCH /api/v1/users/me/profile [Bearer, All, Org: JWT] -> UserProfileDto (Reuse Direct)
- PUT /api/v1/users/me/change-password [Bearer, All] -> MessageResponseDto (Reuse Direct)
- GET /api/v1/organizations [Bearer, All] -> OrganizationListDto[] (Reuse Direct)
- POST /api/v1/organizations/switch [Bearer, All] -> SwitchOrgResponseDto (Reuse Direct)
- GET /api/v1/roles/permissions [Bearer, All, Org: Header] -> RolePermissionsDto (Reuse Direct)

### 3.3 Customer & Project Master Data (Categories 8 - 9)
- GET /api/v1/customers [Bearer, SA/OA/MM/LO, Org: Header, Paging: Yes] -> PaginatedResponse<CustomerDto> (Reuse Direct)
- GET /api/v1/customers/:id [Bearer, SA/OA/MM/LO, Org: Header] -> CustomerDetailDto (Reuse Direct)
- GET /api/v1/projects [Bearer, SA/OA/MM/LO, Org: Header, Paging: Yes] -> PaginatedResponse<ProjectDto> (Reuse Direct)
- GET /api/v1/projects/:id [Bearer, SA/OA/MM/LO, Org: Header] -> ProjectDetailDto (Reuse Direct)

### 3.4 Trolley Catalog, 360 View & QR Scanning (Categories 10 - 13)
- GET /api/v1/trolleys [Bearer, All, Org: Header, Paging: Yes] -> PaginatedResponse<TrolleySummaryDto> (Reuse Direct)
- GET /api/v1/trolleys/:id [Bearer, All, Org: Header] -> TrolleyFullDetailDto (Optimizable)
- GET /api/v1/trolleys/:id/360 [Bearer, All, Org: Header] -> Trolley360ViewDto (Reuse Direct)
- GET /api/v1/trolleys/scan/:barcode [Bearer, All, Org: Header] -> TrolleyQuickScanDto (Reuse Direct - High Mobile Value)
- POST /api/v1/trolleys/register [Bearer, SA/OA/MM, Org: Header] -> TrolleySummaryDto (Reuse Direct)
- PATCH /api/v1/trolleys/:id/status [Bearer, SA/OA/MM/LO, Org: Header] -> TrolleySummaryDto (Reuse Direct)

### 3.5 Preventive Maintenance (PM) & Checklists (Categories 14 - 16)
- GET /api/v1/pm/schedules [Bearer, SA/OA/MM/MT, Org: Header, Paging: Yes] -> PaginatedResponse<PmScheduleDto> (Reuse Direct)
- GET /api/v1/pm/schedules/:id [Bearer, SA/OA/MM/MT, Org: Header] -> PmScheduleDetailDto (Reuse Direct)
- GET /api/v1/pm/checklists/templates/:typeId [Bearer, SA/OA/MM/MT, Org: Header] -> ChecklistTemplateDto (Reuse Direct)
- POST /api/v1/pm/executions [Bearer, MM/MT, Org: Header] -> PmExecutionResultDto (Reuse Direct)
- GET /api/v1/pm/executions/:id [Bearer, SA/OA/MM/MT, Org: Header] -> PmExecutionHistoryDto (Reuse Direct)

### 3.6 Damage & Incident Management (Categories 17 - 18)
- POST /api/v1/damages [Bearer, All except VI, Org: Header] -> DamageReportDto (Reuse Direct - Core Mobile Flow)
- GET /api/v1/damages [Bearer, All, Org: Header, Paging: Yes] -> PaginatedResponse<DamageReportSummaryDto> (Reuse Direct)
- GET /api/v1/damages/:id [Bearer, All, Org: Header] -> DamageReportDetailDto (Reuse Direct)
- PATCH /api/v1/damages/:id/status [Bearer, SA/OA/MM, Org: Header] -> DamageReportDto (Reuse Direct)

### 3.7 Work Orders, Assignments & Roles Workflow (Categories 19 - 21)
- POST /api/v1/maintenance/work-orders [Bearer, SA/OA/MM, Org: Header] -> WorkOrderDto (Reuse Direct)
- GET /api/v1/maintenance/work-orders/my [Bearer, MT, Org: Header, Paging: Yes] -> PaginatedResponse<WorkOrderSummaryDto> (Reuse Direct)
- PATCH /api/v1/maintenance/work-orders/:id/action [Bearer, MT/MM, Org: Header] -> WorkOrderDto (Reuse Direct)
- GET /api/v1/users/technicians [Bearer, SA/OA/MM, Org: Header] -> TechnicianListDto[] (Reuse Direct)

### 3.8 Logistics, Movement & Gate IN/OUT (Categories 22 - 23)
- POST /api/v1/logistics/movement/gate-in [Bearer, SA/OA/LO, Org: Header] -> MovementLogDto (Reuse Direct)
- POST /api/v1/logistics/movement/gate-out [Bearer, SA/OA/LO, Org: Header] -> MovementLogDto (Reuse Direct)
- GET /api/v1/logistics/movement/history [Bearer, All, Org: Header, Paging: Yes] -> PaginatedResponse<MovementLogDto> (Reuse Direct)
- POST /api/v1/logistics/movement/batch [Bearer, SA/OA/LO, Org: Header] -> BatchMovementResultDto (Reuse Direct - Batch Scan)

### 3.9 Reports, Dashboard & Analytics (Categories 24 - 25)
- GET /api/v1/dashboard/kpis [Bearer, All, Org: Header] -> DashboardKpiDto (Reuse Direct)
- GET /api/v1/dashboard/fleet-utilization [Bearer, SA/OA/MM/LO, Org: Header] -> FleetUtilizationDto (Reuse Direct)
- GET /api/v1/reports/trolley-movement/export [Bearer, SA/OA/MM] -> Binary Stream (Do Not Expose to Mobile)
- GET /api/v1/reports/maintenance-summary/export [Bearer, SA/OA/MM] -> Binary Stream (Do Not Expose to Mobile)

### 3.10 Notifications, Media Upload & Global Search (Categories 26 - 29)
- GET /api/v1/notifications [Bearer, All, Org: Header, Paging: Yes] -> PaginatedResponse<NotificationItemDto> (Reuse Direct)
- PATCH /api/v1/notifications/:id/read [Bearer, All, Org: Header] -> NotificationItemDto (Reuse Direct)
- PATCH /api/v1/notifications/read-all [Bearer, All, Org: Header] -> MessageResponseDto (Reuse Direct)
- POST /api/v1/media/upload [Bearer, All, Org: Header] -> MediaUploadResponseDto (Reuse Direct - Multipart)
- GET /api/v1/search/global [Bearer, All, Org: Header] -> GlobalSearchResultDto (Reuse Direct)

### 3.11 Error Envelope (Category 30)
Standard NestJS exception envelope:
\`\`\`json
{
  "statusCode": 400,
  "timestamp": "2026-09-08T12:00:00.000Z",
  "path": "/api/v1/logistics/movement/gate-in",
  "error": "Bad Request",
  "message": ["trolleyId must be a valid UUID"]
}
\`\`\`

### 3.12 Authorization & Security (Categories 31 - 32)
- Mandatory header: x-organization-id: <uuid>
- Server-side validation: TenantGuard cross-checks user membership before processing.`
  },
  {
    id: 'api-gap-analysis',
    name: 'mobile-api-gap-analysis.md',
    path: 'docs/mobile-api-gap-analysis.md',
    description: 'Field gap assessment, exclusions, server validation rules, and file upload pipelines.',
    content: `# AutoLogix TMS - Mobile API Gap Analysis & Field Optimization

**Document Reference:** docs/mobile-api-gap-analysis.md  
**System:** AutoLogix Trolley Management System (TMS)  
**Target Client:** Native Android Mobile Application (mobile-app_v1)  
**Backend:** NestJS REST APIs  
**Author:** Senior Android Solution Architect & Lead Developer  
**Status:** Baseline Documentation (Awaiting Approval)

---

## 1. Executive Summary

While the existing AutoLogix TMS NestJS backend exposes rich, feature-complete REST APIs serving the Angular desktop application, native Android mobile operations introduce unique field constraints:
1. Harsh Factory & Yard Environment (intermittent LTE/Wi-Fi packet loss)
2. High-Speed Gate Scanning (truck with 45 trolleys scanned continuously in 60 seconds)
3. Hardware Integration (ML Kit / CameraX, image compression, FCM push)

---

## 2. Identified API Gaps & Recommended Mobile Endpoints

1. **GAP-01: FCM Device Registration**
   - No endpoint to register Android FCM device tokens for push alerts.
   - Recommended additive endpoint: POST /api/v1/devices/register { fcmToken, deviceModel, appVersion, osVersion }.
2. **GAP-02: Continuous Batch Barcode Verification**
   - Dock operators scan raw barcodes sequentially.
   - Solution: Client-side offline accumulation queue + POST /logistics/movement/batch with zero backend changes.
3. **GAP-03: Offline Delta Sync for Checklists**
   - Deep basement plant zones lose connectivity.
   - Solution: Android Room database + WorkManager sync worker.
4. **GAP-04: Image Pre-Compression & Resizing**
   - Raw 12MP camera shots are 4MB-12MB.
   - Solution: Client-side downsample to 1600x1200 WebP/JPEG (<500KB) before POST /media/upload.

---

## 3. Targeted API Categorization

### 3.1 APIs That Should NOT Be Exposed to Mobile
- POST /admin/tenants (Tenant provisioning is web-only IT desk operation)
- POST /admin/database/seed (Diagnostic/migration utility)
- GET /reports/*/export (50MB+ multi-sheet Excel/PDF files)
- POST /trolleys/bulk-import-csv (Desktop spreadsheet import)
- DELETE /organizations/:id (Disaster recovery risk on mobile)

### 3.2 APIs Requiring Additional Authorization
- Damage Write-Off / Scrap Status (PATCH /damages/:id/status with SCRAP): Requires Maintenance Manager role + supervisor PIN confirmation.
- Gate Bypass / Overriding Status Mismatch: Mandatory supervisor override remark audited with user ID.

### 3.3 APIs Requiring Strict Server-Side Validation
- POST /logistics/movement/gate-in: Ensure trolley exists in org, valid condition status, regex check on truck number.
- POST /pm/executions: Schedule must be ASSIGNED or IN_PROGRESS; mandatory checklist items verified.
- POST /damages: Photo URL required if severity == CRITICAL; auto-transition trolley state to DAMAGED.

### 3.4 APIs Requiring File Upload Handling
- POST /media/upload: multipart/form-data. Client downsizes to 1600x1200 JPEG quality 80 before sending.

### 3.5 APIs Requiring Pagination
- GET /trolleys (limit: 25)
- GET /maintenance/work-orders/my (limit: 15)
- GET /damages (limit: 20)
- GET /logistics/movement/history (limit: 30)
- GET /notifications (limit: 20)`
  },
  {
    id: 'security-analysis',
    name: 'mobile-security-analysis.md',
    path: 'docs/mobile-security-analysis.md',
    description: 'Hardware Keystore, EncryptedSharedPreferences, OkHttp Authenticator, and Multi-Tenant Isolation.',
    content: `# AutoLogix TMS - Mobile Security & Multi-Organization Architecture

**Document Reference:** docs/mobile-security-analysis.md  
**System:** AutoLogix Trolley Management System (TMS)  
**Target Client:** Native Android Mobile Application (mobile-app_v1)  
**Backend:** NestJS Multi-Tenant Engine  
**Author:** Senior Android Solution Architect & Lead Developer  
**Status:** Baseline Documentation (Awaiting Approval)

---

## 1. Security Architecture Principles

Adheres to the Zero Trust Device & Defense-in-Depth model:
- Hardware Keystore (TEE) with MasterKey AES256-GCM
- EncryptedSharedPreferences (Jetpack Security) storing tokens and tenant ID
- Biometric Prompt (BiometricManager) for shift quick-unlock
- OkHttp Client with TLS 1.3 + Certificate Pinning + AuthInterceptor
- NestJS Gateway Ingress with JwtAuthGuard, TenantGuard, and RolesGuard

---

## 2. Multi-Organization Data Isolation

1. Tenant Context Propagation:
   - Client stores active organizationId in encrypted preferences.
   - Attached to all HTTP requests:
     \`\`\`http
     Authorization: Bearer <jwt_access_token>
     x-organization-id: 3fa85f64-5717-4562-b3fc-2c963f66afa6
     x-app-version: 1.0.0
     x-client-platform: ANDROID
     \`\`\`
2. NestJS TenantGuard verifies user belongs to x-organization-id.

---

## 3. Token Lifecycle & Automated Refresh

- Access Token: 15 minutes TTL, JWT format.
- Refresh Token: 7 days TTL.
- OkHttp Authenticator synchronizes thread execution to prevent refresh storms on 401 Unauthorized.

---

## 4. On-Device Storage & Network Security

- No plaintext SharedPreferences.
- EncryptedSharedPreferences backed by hardware Keystore.
- Room SQLite database encrypted via SQLCipher.
- Cleartext traffic disabled in AndroidManifest.xml.
- Certificate Pinning defined in res/xml/network_security_config.xml.`
  },
  {
    id: 'architecture',
    name: 'mobile-architecture.md',
    path: 'docs/mobile-architecture.md',
    description: 'Android Clean Architecture, Jetpack Compose, CameraX + ML Kit scanning, and Room sync queue.',
    content: `# AutoLogix TMS - Mobile Technical Architecture & Engineering Design

**Document Reference:** docs/mobile-architecture.md  
**System:** AutoLogix Trolley Management System (TMS)  
**Target Path:** C:\\repo\\Autologix-TMS\\mobile-app_v1  
**Application:** Native Android Companion Application  
**Author:** Senior Android Solution Architect & Lead Developer  
**Status:** Architectural Specification (Awaiting Implementation Approval)

---

## 1. Architectural Principles & Overview

1. Zero Direct DB Access: Strictly consumes existing NestJS REST APIs.
2. Business Rule Preservation: Backend validates state machine rules.
3. Hardware Agility: CameraX + Google ML Kit optical barcode scanning + physical laser scanner intent wedges (Zebra/Honeywell).
4. Unidirectional Data Flow (MVI/UDF): Jetpack Compose + StateFlow + SharedFlow.

---

## 2. Technology Stack

- Language: Kotlin 2.0+
- UI: Jetpack Compose (BOM 2024+), Material 3
- Architecture: MVVM / Clean Architecture (Presentation, Domain, Data)
- DI: Dagger Hilt
- Async: Kotlin Coroutines & Flow
- Networking: Retrofit 2 + OkHttp 4 + Kotlinx Serialization
- Database: Room 2.6+ (SQLCipher)
- Security: Jetpack Security (EncryptedSharedPreferences)
- Barcode/QR: CameraX + Google ML Kit Barcode Scanning
- Background: WorkManager (Offline sync)
- Push: Firebase Cloud Messaging (FCM)

---

## 3. Package Structure

\`\`\`
com.autologix.tms/
├── core/
│   ├── network/       # Retrofit, AuthInterceptor, Authenticator
│   ├── security/      # SecureTokenStorage, KeystoreManager
│   └── util/          # BarcodeParser, ImageCompressor
├── data/
│   ├── remote/        # Retrofit Services (Auth, Trolley, PM, Gate)
│   ├── local/         # Room Database, OfflineQueueDao
│   └── repository/    # Repository Implementations
├── domain/
│   ├── model/         # Pure Kotlin Data Classes (Trolley, WorkOrder)
│   ├── repository/    # Clean Repository Interfaces
│   └── usecase/       # Granular Use Cases
├── presentation/
│   ├── navigation/    # Role-guarded NavGraph
│   ├── trolley/       # Trolley 360, Catalog, ViewModel
│   ├── logistics/     # Gate IN/OUT, Batch Scan
│   ├── maintenance/   # PM Checklist, Work Orders
│   └── damage/        # Incident Report, Camera Capture
└── service/
    ├── sync/          # WorkManager SyncWorker
    └── push/          # TmsFirebaseMessagingService
\`\`\``
  },
  {
    id: 'development-plan',
    name: 'mobile-development-plan.md',
    path: 'docs/mobile-development-plan.md',
    description: 'Sprint-by-sprint development roadmap, testing strategy, risk matrix, and governance gate.',
    content: `# AutoLogix TMS - Mobile Companion Development & Implementation Plan

**Document Reference:** docs/mobile-development-plan.md  
**System:** AutoLogix Trolley Management System (TMS)  
**Target Path:** C:\\repo\\Autologix-TMS\\mobile-app_v1  
**Target Platform:** Native Android (Kotlin + Jetpack Compose)  
**Author:** Senior Android Solution Architect & Lead Developer  
**Status:** Strategic Roadmap (Awaiting Stakeholder Approval Prior to Code Execution)

---

## 1. Plan Overview & Governance Directives

- Zero Disruption to Existing Stack: Existing Angular frontend and NestJS backend remain untouched.
- Pure API Consumption: No new databases, no direct SQL queries, no duplicated business rules.
- Strict Gate Review: Implementation commences only after formal stakeholder sign-off on architecture docs.

---

## 2. Phased Implementation Roadmap

- Phase 0: Project Scaffolding & Infrastructure (Sprint 1)
  - Gradle Version Catalog (libs.versions.toml), Clean Architecture skeleton in C:\\repo\\Autologix-TMS\\mobile-app_v1, OkHttp client, Keystore security wrapper.
- Phase 1: Authentication & Multi-Tenancy (Sprint 2)
  - Login screen, Organization selection, silent token refresh, Biometric prompt, Role-based navigation.
- Phase 2: Barcode/QR Scanning & Trolley 360 (Sprint 3)
  - CameraX + ML Kit scanning, hardware laser wedge, Trolley 360 view, paginated fleet catalog.
- Phase 3: Logistics & Gate IN/OUT Rapid Operations (Sprint 4)
  - Gate IN/OUT screens, batch scan accumulator, Room database offline sync queue.
- Phase 4: Maintenance Work Orders & Damage Incidents (Sprint 5)
  - Technician work orders, dynamic PM checklist runner, camera damage reporting with image compression.
- Phase 5: Notifications, Offline Sync & Enterprise Hardening (Sprint 6)
  - FCM push alerts, WorkManager sync worker, Executive KPI dashboard, certificate pinning.
- Phase 6: Field Pilot, UAT & Production Rollout (Sprint 7)
  - Factory floor UAT, low-light scan benchmarking, ProGuard obfuscation, MDM release.

---

## 3. Risk Analysis & Mitigation Matrix

- R1 (Wi-Fi Drops at Gates): Room offline queue + WorkManager sync.
- R2 (12MP Photo Bandwidth Exhaustion): Client-side WebP/JPEG compression (<500KB).
- R3 (Dirty Barcode Plates): Multi-format ML Kit engine + manual serial number fallback.
- R4 (Token Expiration During Uploads): Synchronous OkHttp TokenAuthenticator.
- R5 (Cross-Tenant Data Leakage): x-organization-id header injection + NestJS TenantGuard.`
  }
];
