# AutoLogix TMS - Mobile Companion Development & Implementation Plan

**Document Reference:** `docs/mobile-development-plan.md`  
**System:** AutoLogix Trolley Management System (TMS)  
**Target Path:** `C:\repo\Autologix-TMS\mobile-app_v1`  
**Target Platform:** Native Android (Kotlin + Jetpack Compose)  
**Author:** Senior Android Solution Architect & Lead Developer  
**Status:** Strategic Roadmap (Awaiting Stakeholder Approval Prior to Code Execution)

---

## 1. Plan Overview & Governance

This engineering plan defines the phased roadmap for constructing the native Android companion application for AutoLogix TMS within `C:\repo\Autologix-TMS\mobile-app_v1`. 

### Governance Directives:
* **Zero Disruption to Existing Stack:** The existing Angular frontend and NestJS backend remain untouched in production.
* **Pure API Consumption:** No new databases, no direct SQL Server queries, and no duplicated business rules.
* **Strict Gate Review:** Implementation will commence only after formal stakeholder sign-off on the API inventory, gap analysis, security architecture, and this development plan.

---

## 2. Phased Implementation Roadmap

```
+-----------------------------------------------------------------------------------------+
|                               PHASED DELIVERY SCHEDULE                                 |
+-------------------+--------------------+--------------------+---------------------------+
| Phase 0: Baseline | Phase 1: Core Auth | Phase 2: Trolley   | Phase 3: Logistics        |
| & Scaffolding     | & Multi-Tenancy    | 360 & Barcode      | & Gate IN/OUT             |
| (Sprint 1)        | (Sprint 2)         | (Sprint 3)         | (Sprint 4)                |
+-------------------+--------------------+--------------------+---------------------------+
                                                              |
+-------------------------------------------------------------+---------------------------+
| Phase 4: Maintenance & Damage   | Phase 5: Notifications, Sync | Phase 6: Pilot, UAT    |
| Operations                      | & Enterprise Hardening       | & Rollout                 |
| (Sprint 5)                      | (Sprint 6)                   | (Sprint 7)                |
+---------------------------------+------------------------------+---------------------------+
```

---

### Phase 0: Project Scaffolding & Engineering Infrastructure (Sprint 1)
* **Goal:** Establish a pristine, production-grade Android project skeleton in `C:\repo\Autologix-TMS\mobile-app_v1`.
* **Deliverables:**
  1. Initialize Gradle Version Catalog (`gradle/libs.versions.toml`) with Kotlin 2.0, Compose BOM, Hilt, Retrofit, Room, and Coroutines.
  2. Configure build flavors (`dev`, `staging`, `prod`) targeting existing NestJS environments.
  3. Implement base network module: OkHttp client with `AuthInterceptor`, `NetworkStateInterceptor`, and `TokenAuthenticator`.
  4. Implement hardware-backed `SecureTokenStorage` utilizing Android Keystore and `EncryptedSharedPreferences`.
  5. Setup static analysis tools: Detekt, ktlint, and Android Lint.

---

### Phase 1: Authentication, Multi-Tenancy & Session Management (Sprint 2)
* **Goal:** Enable secure user login, role extraction, dynamic organization switching, and biometrics.
* **Deliverables:**
  1. Material 3 Login Screen with corporate branding, username/password validation, and password visibility toggles.
  2. Implement `AuthRepository` and `LoginUseCase` consuming `POST /auth/login`.
  3. Multi-Organization Selection Screen consuming `GET /organizations` and `POST /organizations/switch`.
  4. Silent token refresh mechanism handling HTTP 401 transparently via OkHttp.
  5. Biometric authentication prompt (`BiometricManager`) for rapid app re-entry during shifts.
  6. Dynamic role-based navigation scaffolding (routes guarded by `UserRole`).

---

### Phase 2: Barcode/QR Scanning & Trolley 360 Module (Sprint 3)
* **Goal:** Provide instant field visibility into trolley specifications, location, and maintenance state.
* **Deliverables:**
  1. CameraX scanning viewfinder with Google ML Kit Barcode Scanning (QR, Code 128, DataMatrix).
  2. Laser hardware intent wedge listener for Zebra and Honeywell enterprise rugged scanners.
  3. Instant scan resolver consuming `GET /trolleys/scan/:barcode`.
  4. Trolley 360 View Screen:
     - Header: Serial Number, Type, Current Status chip, Current Location badge.
     - Tabs: Specifications, Recent Gate Movements, Active Damage Tickets, PM Audit History.
  5. Trolley Catalog with search, filter chips (Yard, In Transit, Damaged), and Jetpack Paging 3.

---

### Phase 3: Logistics & Gate IN/OUT Rapid Operations (Sprint 4)
* **Goal:** Streamline dock receiving and truck dispatch with rapid batch scanning.
* **Deliverables:**
  1. Gate IN Screen:
     - Scan trolley barcode -> Auto-populate specs.
     - Truck number, gate pass number, origin location, condition assessment.
     - Submit to `POST /logistics/movement/gate-in`.
  2. Gate OUT Screen:
     - Destination plant selector, dispatch challan / trip number.
     - Submit to `POST /logistics/movement/gate-out`.
  3. Continuous Batch Scan Mode:
     - Fast multi-scan list accumulator.
     - One-touch dispatch calling `POST /logistics/movement/batch`.
  4. Movement audit history feed consuming `GET /logistics/movement/history`.

---

### Phase 4: Maintenance Work Orders & Damage Incidents (Sprint 5)
* **Goal:** Empower floor technicians to execute PM checklists and log damages with photographic proof.
* **Deliverables:**
  1. Technician "My Work Orders" dashboard consuming `GET /maintenance/work-orders/my`.
  2. Interactive PM Checklist Runner:
     - Dynamically rendered checklist items from `GET /pm/checklists/templates/:typeId`.
     - Pass/Fail toggles, remarks input, mandatory photo capture.
     - Digital supervisor signature capture canvas.
     - Submit to `POST /pm/executions`.
  3. Damage Reporting Flow:
     - CameraX integration with client-side JPEG/WebP compression (< 500KB).
     - Multipart upload to `/media/upload`.
     - Severity picker (Minor, Major, Critical) and incident report submission to `POST /damages`.
  4. Manager Review & Work Order Assignment screens for Maintenance Managers.

---

### Phase 5: Notifications, Offline Sync & Enterprise Hardening (Sprint 6)
* **Goal:** Ensure resilient offline operations and instant real-time alerting.
* **Deliverables:**
  1. Firebase Cloud Messaging (FCM) integration with high-priority channel notifications for critical damages.
  2. Room SQLite offline queue for Gate movements and PM audits captured without network.
  3. Android `WorkManager` background sync worker with automatic retry and exponential backoff.
  4. Executive Dashboard with KPIs consuming `GET /dashboard/kpis` (utilization charts, yard counts).
  5. Security hardening: SafetyNet / Play Integrity verification, Certificate Pinning (`network_security_config.xml`).

---

### Phase 6: Field Pilot, UAT & Production Rollout (Sprint 7)
* **Goal:** Validate on physical devices in actual plant conditions and deploy.
* **Deliverables:**
  1. User Acceptance Testing (UAT) with logistics dock operators and maintenance technicians.
  2. Benchmark camera scanning speed under low-light and dirty barcode conditions.
  3. APK / AAB generation with ProGuard / R8 code shrinking and obfuscation.
  4. Enterprise MDM deployment package (Microsoft Intune / SOTI MobiControl / Google Play Private Track).

---

## 3. Testing Strategy & Quality Gates

| Test Layer | Frameworks & Tools | Coverage Target | Focus Area |
| :--- | :--- | :---: | :--- |
| **Unit Tests** | JUnit 5, MockK, Turbine, Coroutines Test | > 85% | ViewModels, Use Cases, Repositories, Token Authenticator, Barcode Parsers. |
| **Integration Tests** | MockWebServer, Room In-Memory DB | > 80% | Offline sync queue, Retrofit DTO parsing, NestJS error response handling. |
| **UI Tests** | Compose Test Rule, Robolectric | > 70% | Screen state transitions (Loading, Success, Error), Checklist inputs. |
| **Device Smoke Tests** | Physical Android 10, 12, 14 devices | 100% Core Flows | CameraX scan accuracy, Keystore crypto persistence, Memory leak checks. |

---

## 4. Risk Analysis & Mitigation Matrix

| # | Identified Risk | Severity | Probability | Mitigation Strategy |
|---|---|:---:|:---:|---|
| **R1** | Intermittent Wi-Fi drops at factory gates cause failed movement logs. | **High** | High | Room-based offline queue (`OfflineSyncQueueEntity`) + WorkManager background sync. |
| **R2** | Uploading full 12MP camera photos exhausts cellular bandwidth. | **High** | High | Client-side image compression pipeline enforcing 1600x1200 max resolution and 80% JPEG quality. |
| **R3** | Barcode scanning fails on dirty or damaged metal trolley plates. | **Medium** | High | Multi-format ML Kit engine + fallback manual serial number keyboard entry with instant autocomplete. |
| **R4** | Concurrent token expiration during multi-image uploads. | **Medium** | Medium | OkHttp thread-safe `TokenAuthenticator` with atomic refresh lock. |
| **R5** | Cross-tenant data leakage on shared handheld devices. | **Critical** | Low | Explicit `x-organization-id` header injection + backend NestJS `TenantGuard` cross-validation. |

---

## 5. Architectural Approval Gate

> **CRITICAL CHECKPOINT:**  
> In compliance with user instructions, all preliminary analysis, 32-category API mapping, security architecture, and phased execution plans are finalized in the `docs/` repository.  
> **NO PRODUCTION CODE OR IMPLEMENTATION WILL BE EXECUTED UNTIL FORMAL STAKEHOLDER APPROVAL IS GRANTED.**
