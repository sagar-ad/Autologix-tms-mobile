# AutoLogix TMS - Mobile API Gap Analysis & Field Optimization

**Document Reference:** `docs/mobile-api-gap-analysis.md`  
**System:** AutoLogix Trolley Management System (TMS)  
**Target Client:** Native Android Mobile Application (`mobile-app_v1`)  
**Backend:** NestJS REST APIs  
**Author:** Senior Android Solution Architect & Lead Developer  
**Status:** Baseline Documentation (Awaiting Approval)

---

## 1. Executive Summary

While the existing AutoLogix TMS NestJS backend exposes rich, feature-complete REST APIs serving the Angular desktop application, native Android mobile operations introduce unique field constraints:
1. **Harsh Factory & Yard Environment:** Forklifts, dock doors, concrete walls causing intermittent LTE/Wi-Fi packet loss.
2. **High-Speed Gate Scanning:** A truck arrives with 45 automotive dollies/trolleys; security/dock operators must scan them continuously within 60 seconds.
3. **Hardware Integration:** ML Kit / CameraX continuous barcode scanning, on-device image compression before uploading 12MP camera photos, and native FCM push notification handling.

This gap analysis specifies:
- Identified API functional gaps.
- Non-breaking mobile extensions or client-side adapters.
- Categorization of APIs regarding exposure, authorization, validation, uploads, pagination, and response optimization.

---

## 2. Identified API Gaps & Recommended Mobile Endpoints

| Gap ID | Identified Functional Gap | Mobile Impact | Recommended Solution | Backend Impact |
| :--- | :--- | :--- | :--- | :--- |
| **GAP-01** | **FCM Device Registration**<br>No dedicated endpoint to register Android Firebase Cloud Messaging device tokens for push alerts. | Mobile users cannot receive push notifications for urgent work orders, PM reminders, or critical damages. | Introduce: `POST /api/v1/devices/register`<br>Payload: `{ fcmToken, deviceModel, appVersion, osVersion }`. | **Additive Only** (New Controller or sub-route). Can be queued in Phase 5 without blocking core app. |
| **GAP-02** | **Continuous Batch Barcode Verification**<br>Existing `/logistics/movement/batch` requires pre-selected IDs, but dock operators scan raw QR/Barcodes one by one. | Android must either call `/trolleys/scan/:barcode` 40 times sequentially (latency bottleneck) or verify in batch. | Introduce client-side offline accumulation queue + single call to `/logistics/movement/batch` with mapped IDs, or add `POST /trolleys/scan-batch` taking `barcodes: string[]`. | Client-side queue handles this with zero backend change! |
| **GAP-03** | **Offline Delta Sync for Checklists**<br>No timestamp-filtered sync endpoint (`/sync/delta?since=timestamp`). | Technicians in deep plant basements or metal storage yards lose network during PM audits. | Client implements local SQLite/Room database with offline sync queue. When back online, Android WorkManager flushes pending PM executions and gate movements. | Client-side capability using WorkManager + Room. |
| **GAP-04** | **Image Pre-Compression & Resizing**<br>Raw Android camera shots are 4MB - 12MB. Uploading 4 photos per damage report consumes 48MB. | Factory LTE upload times exceed 30s or timeout. | Android client-side image processing pipeline (compress to WebP/JPEG 1600x1200, < 600KB) before submitting to `/media/upload`. | **Zero backend changes needed.** Handled entirely in mobile domain layer. |

---

## 3. Targeted API Categorization

### 3.1 APIs That Should NOT Be Exposed to Mobile

These endpoints exist for web administration, batch database ingestion, or system reporting and MUST NOT be incorporated into the mobile application:

| Endpoint | Reason for Mobile Exclusion | Threat/Usability Risk |
| :--- | :--- | :--- |
| `POST /admin/tenants` | Tenant provisioning is exclusively an IT/Super-Admin web desk operation. | Unauthorized multi-tenant manipulation. |
| `POST /admin/database/seed` | Diagnostic/migration utility. | Extreme data integrity risk. |
| `GET /reports/*/export` | Generates 50MB+ multi-sheet Excel / high-res PDF files with desktop styling. | Device out-of-memory, massive cellular data waste. |
| `POST /trolleys/bulk-import-csv` | CSV ingestion of 5,000 trolley records from ERP spreadsheets. | File picker ergonomics and transaction timeout on mobile. |
| `DELETE /organizations/:id` | Irreversible tenant teardown. | Disaster recovery risk on mobile touch devices. |

---

### 3.2 APIs Requiring Additional Authorization (MFA / Manager Overrides)

Certain high-impact actions on the factory floor must enforce explicit role-checks or supervisor overrides:

1. **Damage Write-Off / Scrap Status (`PATCH /damages/:id/status` with `SCRAP`):**
   * *Requirement:* Only `SUPER_ADMIN` or `ORG_ADMIN` with `MAINTENANCE_MANAGER` role.
   * *Mobile Enforcement:* Guard action button behind role check; display supervisor biometric re-auth or PIN confirmation dialog.
2. **Gate Bypass / Overriding Status Mismatch:**
   * *Scenario:* Trolley marked as `IN_YARD` physically arrives at the gate from outside.
   * *Mobile Enforcement:* Force user to enter mandatory "Supervisor Override Reason" in `GateInDto.remarks`, auditing the user ID.

---

### 3.3 APIs Requiring Strict Server-Side Validation

The Android app provides instantaneous client-side form validation (Jetpack Compose), but the backend NestJS `ValidationPipe` must strictly enforce:

| Target API | Critical Validation Rules |
| :--- | :--- |
| `POST /logistics/movement/gate-in` | • `trolleyId` must exist and belong to `x-organization-id`<br>• Cannot Gate-IN a trolley already flagged `UNDER_MAINTENANCE` without clear inspection flag<br>• `truckNumber` must match regex `^[A-Z0-9\-\s]{4,15}$` |
| `POST /pm/executions` | • `scheduleId` must be `ASSIGNED` or `IN_PROGRESS`<br>• Every mandatory checklist item must have a boolean status<br>• If an item condition is `FAILED`, `remarks` must not be empty |
| `POST /damages` | • If severity is `CRITICAL`, `photoUrls` must contain at least 1 image<br>• `trolleyId` status must automatically transition to `DAMAGED` |

---

### 3.4 APIs Requiring File Upload Handling

Field operations frequently require photographic evidence. The mobile client will interface with `/media/upload`:

* **Upload Mechanism:** Standard `multipart/form-data` with `file: File` and `tag: DAMAGE | PM | GATE`.
* **Mobile Image Pipeline:**
  1. Capture raw bitmap from CameraX.
  2. Write EXIF orientation tags and GPS metadata (if authorized).
  3. Downsample to max dimensions 1920x1080.
  4. Compress using `Bitmap.CompressFormat.JPEG` at quality 80 (resulting in ~350KB - 600KB file).
  5. Multi-part POST with OkHttp progress listener showing upload percentage in Compose UI.
  6. Return generated CDN URL and bind into the corresponding domain payload (`photoUrls[]`).

---

### 3.5 APIs Requiring Pagination

To preserve mobile RAM and minimize network payload, all list screens MUST implement pagination:

| Screen | Target API | Pagination Strategy | Default Limit |
| :--- | :--- | :--- | :--- |
| Trolley Catalog | `GET /trolleys` | Page-based (`page=1&limit=25`) | 25 items |
| My Work Orders | `GET /maintenance/work-orders/my` | Page-based (`page=1&limit=15`) | 15 items |
| Damage Tickets | `GET /damages` | Page-based (`page=1&limit=20`) | 20 items |
| Gate Movement History | `GET /logistics/movement/history` | Page-based (`page=1&limit=30`) | 30 items |
| Notifications Feed | `GET /notifications` | Page-based (`page=1&limit=20`) | 20 items |

* **Android Implementation:** Jetpack Paging 3 library with `PagingSource<Int, TrolleyItem>` integrated into Compose `LazyColumn.collectAsLazyPagingItems()`.

---

### 3.6 APIs Requiring Optimized Mobile Responses

Desktop Angular views often fetch complex nested object graphs. For mobile efficiency:
1. **Trolley 360 View (`GET /trolleys/:id/360`):**
   * *Current Response:* Full JSON tree including 100 historical movements, all past work orders, and detailed user audit records (~180KB).
   * *Mobile Strategy:* Request only recent history by passing query params `?recentMovements=5&recentPm=3` (or parse lazily in the Kotlin Data Transfer Object, discarding legacy unneeded fields).
2. **Dashboard KPIs (`GET /dashboard/kpis`):**
   * Lean, numeric summary DTO (~1.5KB) perfectly suited for mobile home screen widgets and cards.

---

## 4. Conclusion & Next Steps

All 47 production NestJS endpoints are intact and unmodified. The Android companion app can fulfill 100% of its operational goals (QR/Barcode scanning, Gate IN/OUT, PM execution, Damage tickets, 360 viewer) using existing endpoints, augmented by robust client-side caching, image compression, and WorkManager background syncing.
