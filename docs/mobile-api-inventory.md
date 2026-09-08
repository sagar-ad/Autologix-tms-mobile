# AutoLogix TMS - Mobile REST API Inventory & Reuse Assessment

**Document Reference:** `docs/mobile-api-inventory.md`  
**System:** AutoLogix Trolley Management System (TMS)  
**Target Client:** Native Android Mobile Application (`mobile-app_v1`)  
**Backend:** NestJS REST APIs (PostgreSQL / MySQL Server / Multi-tenant Engine)  
**Author:** Senior Android Solution Architect & Lead Developer  
**Status:** Review & Architectural Baseline (Awaiting Implementation Approval)

---

## 1. Executive Summary & Inventory Scope

This inventory comprehensively documents **32 functional API categories** across the existing AutoLogix TMS NestJS backend. Each endpoint has been evaluated against mobile field operating realities:
* **Connectivity constraints:** Unstable Wi-Fi/LTE in manufacturing yards and logistics docks.
* **Payload footprint:** Avoiding multi-megabyte over-fetching common in Angular desktop views.
* **Hardware coupling:** Rapid barcode/QR decoding, CameraX capture, and push notification triggers.
* **Multi-tenancy:** Strict isolation via organizational token claims and `x-organization-id` boundary enforcement.

---

## 2. API Classification & Legend

| Field | Description |
| :--- | :--- |
| **HTTP Method** | `GET`, `POST`, `PUT`, `PATCH`, `DELETE` |
| **Endpoint** | Absolute REST path relative to `/api/v1` base route |
| **Auth** | `Public` (No token), `Bearer` (JWT Access Token), `Refresh` (Refresh Token) |
| **RBAC Roles** | `SA` (Super Admin), `OA` (Org Admin), `MM` (Maintenance Manager), `MT` (Maintenance Tech), `LO` (Logistics Operator), `VI` (Viewer/Customer) |
| **Org Req** | `Header` (`x-organization-id`), `JWT` (Embedded in claims), `None` |
| **Pagination** | `Yes` (standard `page`, `limit`, `sortBy`, `sortDir`), `Cursor` (timestamp-based), `No` |
| **Mobile Suitability**| **Direct** (Ready as-is), **Optimizable** (Usable, but needs payload trim or DTO), **Restricted** (Desktop only) |
| **Action** | `Reuse Direct`, `Extend/Wrap`, `Do Not Expose` |

---

## 3. Comprehensive 32-Category API Matrix

### 3.1 Authentication & Session Management (Categories 1 - 4)

| # | HTTP | Endpoint | Request Model | Response Model | Auth | RBAC | Org Req | Paging | Mobile Suitability | Existing Angular Usage | Action |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | `POST` | `/auth/login` | `LoginDto` (`username`/`email`, `password`, `deviceId?`, `devicePlatform?`) | `AuthResponseDto` (`accessToken`, `refreshToken`, `expiresIn`, `user`, `organizations[]`) | Public | All | None | No | **Direct** | Primary login view in `auth.service.ts` | **Reuse Direct** |
| 2 | `POST` | `/auth/refresh` | `RefreshTokenDto` (`refreshToken`) | `TokenPairDto` (`accessToken`, `refreshToken`, `expiresIn`) | Refresh | All | None | No | **Direct** | Angular HTTP interceptor for silent 401 refresh | **Reuse Direct** |
| 3 | `POST` | `/auth/logout` | `LogoutDto` (`refreshToken`, `deviceId?`) | `MessageResponseDto` (`success: boolean`, `message: string`) | Bearer | All | None | No | **Direct** | Session termination in navbar | **Reuse Direct** |
| 4 | `POST` | `/auth/forgot-password` | `ForgotPasswordDto` (`email`) | `MessageResponseDto` | Public | All | None | No | **Direct** | Password reset link trigger | **Reuse Direct** |
| 5 | `POST` | `/auth/reset-password` | `ResetPasswordDto` (`token`, `newPassword`) | `MessageResponseDto` | Public | All | None | No | **Direct** | Deep-link password reset view | **Reuse Direct** |

---

### 3.2 User Profile, Organization & Roles (Categories 5 - 7)

| # | HTTP | Endpoint | Request Model | Response Model | Auth | RBAC | Org Req | Paging | Mobile Suitability | Existing Angular Usage | Action |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 6 | `GET` | `/users/me` | None | `UserProfileDto` (`id`, `firstName`, `lastName`, `email`, `phone`, `avatarUrl`, `roles[]`, `currentOrgId`) | Bearer | All | JWT | No | **Direct** | App state bootstrap on load | **Reuse Direct** |
| 7 | `PATCH` | `/users/me/profile` | `UpdateProfileDto` (`firstName`, `lastName`, `phone`, `avatarUrl?`) | `UserProfileDto` | Bearer | All | JWT | No | **Direct** | User settings dialog | **Reuse Direct** |
| 8 | `PUT` | `/users/me/change-password`| `ChangePasswordDto` (`currentPassword`, `newPassword`) | `MessageResponseDto` | Bearer | All | JWT | No | **Direct** | Security settings tab | **Reuse Direct** |
| 9 | `GET` | `/organizations` | Query: `?status=ACTIVE` | `OrganizationListDto[]` (`id`, `name`, `code`, `logoUrl`, `status`, `trolleyPrefix`) | Bearer | All | None | No | **Direct** | Multi-org switcher dropdown | **Reuse Direct** |
| 10| `POST` | `/organizations/switch` | `SwitchOrgDto` (`organizationId`) | `SwitchOrgResponseDto` (`scopedAccessToken`, `currentOrg`) | Bearer | All | None | No | **Direct** | Tenant selector in top banner | **Reuse Direct** |
| 11| `GET` | `/roles/permissions` | None | `RolePermissionsDto` (`role`, `permissions: string[]`) | Bearer | All | Header | No | **Direct** | Angular route guards and directive permissions | **Reuse Direct** |

---

### 3.3 Customer & Project Master Data (Categories 8 - 9)

| # | HTTP | Endpoint | Request Model | Response Model | Auth | RBAC | Org Req | Paging | Mobile Suitability | Existing Angular Usage | Action |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 12| `GET` | `/customers` | Query: `page`, `limit`, `search`, `status` | `PaginatedResponse<CustomerDto>` (`items`, `total`, `page`, `totalPages`) | Bearer | SA, OA, MM, LO | Header | Yes | **Optimizable** | Customer management table | **Reuse Direct** (use query limit=20) |
| 13| `GET` | `/customers/:id` | Path: `id` | `CustomerDetailDto` (`id`, `name`, `code`, `plantLocations[]`, `projects[]`) | Bearer | SA, OA, MM, LO | Header | No | **Direct** | Customer drilldown view | **Reuse Direct** |
| 14| `GET` | `/projects` | Query: `customerId?`, `status=ACTIVE` | `PaginatedResponse<ProjectDto>` | Bearer | SA, OA, MM, LO | Header | Yes | **Optimizable** | Project allocation selectors | **Reuse Direct** |
| 15| `GET` | `/projects/:id` | Path: `id` | `ProjectDetailDto` (`id`, `name`, `code`, `trolleyTypes[]`, `standards`) | Bearer | SA, OA, MM, LO | Header | No | **Direct** | Project view | **Reuse Direct** |

---

### 3.4 Trolley Catalog, 360 View & QR Scanning (Categories 10 - 13)

| # | HTTP | Endpoint | Request Model | Response Model | Auth | RBAC | Org Req | Paging | Mobile Suitability | Existing Angular Usage | Action |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 16| `GET` | `/trolleys` | Query: `page`, `limit`, `search`, `status`, `type`, `locationId` | `PaginatedResponse<TrolleySummaryDto>` | Bearer | All | Header | Yes | **Direct** | Trolley fleet table | **Reuse Direct** |
| 17| `GET` | `/trolleys/:id` | Path: `id` | `TrolleyFullDetailDto` (Heavy nested object) | Bearer | All | Header | No | **Optimizable** | Trolley single-page inspector | **Reuse Direct** |
| 18| `GET` | `/trolleys/:id/360` | Path: `id` | `Trolley360ViewDto` (`specs`, `location`, `activeDamage`, `pmHistory`, `movementHistory`) | Bearer | All | Header | No | **Direct** | Web 360 interactive view | **Reuse Direct** (High value for mobile) |
| 19| `GET` | `/trolleys/scan/:barcode` | Path: `barcode` (QR/DataMatrix/Barcode) | `TrolleyQuickScanDto` (`id`, `serialNo`, `qrCode`, `type`, `status`, `currentLocation`, `isDamaged`) | Bearer | All | Header | No | **Direct** | Web manual barcode input | **Reuse Direct** (Ideal for CameraX) |
| 20| `POST` | `/trolleys/register` | `CreateTrolleyDto` | `TrolleySummaryDto` | Bearer | SA, OA, MM | Header | No | **Direct** | Add new trolley dialog | **Reuse Direct** |
| 21| `PATCH` | `/trolleys/:id/status` | `UpdateTrolleyStatusDto` (`status`, `remarks`) | `TrolleySummaryDto` | Bearer | SA, OA, MM, LO | Header | No | **Direct** | Quick status change dropdown | **Reuse Direct** |

---

### 3.5 Preventive Maintenance (PM) & Checklists (Categories 14 - 16)

| # | HTTP | Endpoint | Request Model | Response Model | Auth | RBAC | Org Req | Paging | Mobile Suitability | Existing Angular Usage | Action |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 22| `GET` | `/pm/schedules` | Query: `technicianId?`, `status`, `dateFrom`, `dateTo`, `page`, `limit` | `PaginatedResponse<PmScheduleDto>` | Bearer | SA, OA, MM, MT | Header | Yes | **Direct** | Maintenance calendar/table | **Reuse Direct** |
| 23| `GET` | `/pm/schedules/:id` | Path: `id` | `PmScheduleDetailDto` (`id`, `trolley`, `checklistTemplate`, `assignedTech`, `dueDate`) | Bearer | SA, OA, MM, MT | Header | No | **Direct** | Work order detail modal | **Reuse Direct** |
| 24| `GET` | `/pm/checklists/templates/:typeId` | Path: `typeId` (Trolley Type) | `ChecklistTemplateDto` (`sections[]`, `items[]`: `title`, `criteria`, `isMandatory`, `requiresPhoto`) | Bearer | SA, OA, MM, MT | Header | No | **Direct** | PM checklist builder / preview | **Reuse Direct** |
| 25| `POST` | `/pm/executions` | `CreatePmExecutionDto` (`scheduleId`, `trolleyId`, `itemResults[]`, `overallStatus`, `signatureUrl?`) | `PmExecutionResultDto` | Bearer | MM, MT | Header | No | **Direct** | Technician PM audit submit form | **Reuse Direct** |
| 26| `GET` | `/pm/executions/:id` | Path: `id` | `PmExecutionHistoryDto` | Bearer | SA, OA, MM, MT | Header | No | **Direct** | Past PM audit report viewer | **Reuse Direct** |

---

### 3.6 Damage & Incident Management (Categories 17 - 18)

| # | HTTP | Endpoint | Request Model | Response Model | Auth | RBAC | Org Req | Paging | Mobile Suitability | Existing Angular Usage | Action |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 27| `POST` | `/damages` | `ReportDamageDto` (`trolleyId`, `severity`, `category`, `description`, `photoUrls[]`, `reportedLocationId`) | `DamageReportDto` (`id`, `ticketNo`, `status: OPEN`, `createdAt`) | Bearer | All except VI | Header | No | **Direct** | Incident reporting wizard | **Reuse Direct** (Core mobile feature) |
| 28| `GET` | `/damages` | Query: `status`, `severity`, `trolleyId?`, `page`, `limit` | `PaginatedResponse<DamageReportSummaryDto>` | Bearer | All | Header | Yes | **Direct** | Damage tickets table | **Reuse Direct** |
| 29| `GET` | `/damages/:id` | Path: `id` | `DamageReportDetailDto` (`trolley`, `reporter`, `photos[]`, `workOrder?`) | Bearer | All | Header | No | **Direct** | Damage ticket details page | **Reuse Direct** |
| 30| `PATCH` | `/damages/:id/status` | `UpdateDamageStatusDto` (`status: IN_REVIEW/APPROVED/REJECTED`, `comment`) | `DamageReportDto` | Bearer | SA, OA, MM | Header | No | **Direct** | Manager approval buttons | **Reuse Direct** |

---

### 3.7 Work Orders, Assignments & Roles Workflow (Categories 19 - 21)

| # | HTTP | Endpoint | Request Model | Response Model | Auth | RBAC | Org Req | Paging | Mobile Suitability | Existing Angular Usage | Action |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 31| `POST` | `/maintenance/work-orders` | `CreateWorkOrderDto` (`damageId?`, `pmScheduleId?`, `trolleyId`, `assignedTechnicianId`, `priority`, `targetDate`) | `WorkOrderDto` | Bearer | SA, OA, MM | Header | No | **Direct** | Assign technician dialog | **Reuse Direct** |
| 32| `GET` | `/maintenance/work-orders/my` | Query: `status`, `page`, `limit` | `PaginatedResponse<WorkOrderSummaryDto>` | Bearer | MT | Header | Yes | **Direct** | Technician workbench view | **Reuse Direct** (Primary tech screen) |
| 33| `PATCH` | `/maintenance/work-orders/:id/action` | `WorkOrderActionDto` (`action: START/PAUSE/COMPLETE`, `technicianNotes`, `partsReplaced[]`, `photos[]`) | `WorkOrderDto` | Bearer | MT, MM | Header | No | **Direct** | Action buttons on tech view | **Reuse Direct** |
| 34| `GET` | `/users/technicians` | Query: `status=ACTIVE`, `availableOnly?` | `TechnicianListDto[]` (`id`, `name`, `activeWorkOrdersCount`) | Bearer | SA, OA, MM | Header | No | **Direct** | Assignment dropdown | **Reuse Direct** |

---

### 3.8 Logistics, Movement & Gate IN/OUT (Categories 22 - 23)

| # | HTTP | Endpoint | Request Model | Response Model | Auth | RBAC | Org Req | Paging | Mobile Suitability | Existing Angular Usage | Action |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 35| `POST` | `/logistics/movement/gate-in` | `GateInDto` (`trolleyId`, `gatePassNumber`, `sourceLocationId`, `truckNumber`, `driverName`, `conditionStatus`, `remarks`, `photos[]?`) | `MovementLogDto` (`id`, `movementType: IN`, `timestamp`) | Bearer | SA, OA, LO | Header | No | **Direct** | Dock gate receiving form | **Reuse Direct** |
| 36| `POST` | `/logistics/movement/gate-out` | `GateOutDto` (`trolleyId`, `dispatchNumber`, `destinationLocationId`, `truckNumber`, `driverName`, `sealNumber?`) | `MovementLogDto` (`id`, `movementType: OUT`, `timestamp`) | Bearer | SA, OA, LO | Header | No | **Direct** | Dock gate dispatch form | **Reuse Direct** |
| 37| `GET` | `/logistics/movement/history` | Query: `trolleyId?`, `movementType?`, `dateFrom`, `dateTo`, `page`, `limit` | `PaginatedResponse<MovementLogDto>` | Bearer | All | Header | Yes | **Direct** | Dispatch audit log table | **Reuse Direct** |
| 38| `POST` | `/logistics/movement/batch` | `BatchMovementDto` (`type: IN/OUT`, `trolleyIds: string[]`, `manifestNo`, `truckNumber`, `destinationLocationId?`) | `BatchMovementResultDto` (`succeeded[]`, `failed[]`: `{trolleyId, reason}`) | Bearer | SA, OA, LO | Header | No | **Direct** | Bulk dispatch wizard | **Reuse Direct** (Crucial for mobile rapid batch scan) |

---

### 3.9 Reports, Dashboard & Analytics (Categories 24 - 25)

| # | HTTP | Endpoint | Request Model | Response Model | Auth | RBAC | Org Req | Paging | Mobile Suitability | Existing Angular Usage | Action |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 39| `GET` | `/dashboard/kpis` | Query: `timeRange?` (DAY, WEEK, MONTH) | `DashboardKpiDto` (`totalTrolleys`, `inYardCount`, `inTransitCount`, `underMaintenanceCount`, `criticalDamagesCount`, `pmComplianceRate`) | Bearer | All | Header | No | **Direct** | Desktop executive cards | **Reuse Direct** (Mobile home dashboard) |
| 40| `GET` | `/dashboard/fleet-utilization` | None | `FleetUtilizationDto` (`byProject`, `byCustomer`, `byStatus`) | Bearer | SA, OA, MM, LO | Header | No | **Optimizable** | Charts and graphs in web dashboard | **Reuse Direct** (Render with MPAndroidChart / Compose Canvas) |
| 41| `GET` | `/reports/trolley-movement/export`| Query: `dateFrom`, `dateTo`, `format=EXCEL/PDF` | Binary Stream / File download URL | Bearer | SA, OA, MM | Header | No | **Restricted** | Export button on web report table | **Do Not Expose to Mobile** (Provide download link or web redirect) |
| 42| `GET` | `/reports/maintenance-summary/export` | Query: `dateFrom`, `dateTo`, `format=EXCEL/PDF` | Binary Stream | Bearer | SA, OA, MM | Header | No | **Restricted** | Maintenance export button | **Do Not Expose to Mobile** |

---

### 3.10 Notifications, Media Upload & Global Search (Categories 26 - 29)

| # | HTTP | Endpoint | Request Model | Response Model | Auth | RBAC | Org Req | Paging | Mobile Suitability | Existing Angular Usage | Action |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 43| `GET` | `/notifications` | Query: `unreadOnly=true`, `page`, `limit` | `PaginatedResponse<NotificationItemDto>` | Bearer | All | Header | Yes | **Direct** | Notification bell popup | **Reuse Direct** |
| 44| `PATCH` | `/notifications/:id/read` | Path: `id` | `NotificationItemDto` | Bearer | All | Header | No | **Direct** | Mark as read click handler | **Reuse Direct** |
| 45| `PATCH` | `/notifications/read-all` | None | `MessageResponseDto` | Bearer | All | Header | No | **Direct** | "Mark all read" button | **Reuse Direct** |
| 46| `POST` | `/media/upload` | `multipart/form-data` (`file: Binary`, `tag: DAMAGE/PM/GATE`) | `MediaUploadResponseDto` (`url`, `fileKey`, `mimeType`, `size`) | Bearer | All | Header | No | **Direct** | Angular drag-and-drop file upload | **Reuse Direct** (Use Retrofit `@Multipart`) |
| 47| `GET` | `/search/global` | Query: `q: string`, `types?: string[]` | `GlobalSearchResultDto` (`trolleys[]`, `workOrders[]`, `damages[]`) | Bearer | All | Header | No | **Direct** | Top global search bar | **Reuse Direct** |

---

### 3.11 Error Response Structure & Security Specifications (Categories 30 - 32)

#### 3.11.1 Canonical NestJS Error Envelope
Existing NestJS standard exception filter guarantees this consistent JSON body:
```json
{
  "statusCode": 400,
  "timestamp": "2026-09-08T12:00:00.000Z",
  "path": "/api/v1/logistics/movement/gate-in",
  "error": "Bad Request",
  "message": [
    "trolleyId must be a valid UUID",
    "conditionStatus must be one of: GOOD, MINOR_DAMAGE, MAJOR_DAMAGE"
  ]
}
```
* Or for business rule violations (`HttpException` / `ConflictException`):
```json
{
  "statusCode": 409,
  "timestamp": "2026-09-08T12:00:00.000Z",
  "path": "/api/v1/logistics/movement/gate-in",
  "error": "Conflict",
  "message": "Trolley TRL-2024-089 is currently marked as IN_YARD. Cannot perform Gate-IN."
}
```

#### 3.11.2 Multi-Organization Isolation Enforcement
* **Mandatory Header:** All tenant-scoped endpoints require:
  `x-organization-id: <uuid>`
* **Server Verification:** The NestJS `TenantGuard` cross-checks that the `userId` in the JWT token belongs to the organization specified in `x-organization-id`. If mismatched, it returns `403 Forbidden: Invalid organizational context`.

---

## 4. API Suitability & Action Summary

* **Total Existing Endpoints Analyzed:** 47
* **Directly Reusable on Android:** 42 (89.4%)
* **Optimizable with DTO Filters/Paging:** 3 (6.4%)
* **Restricted / Excluded from Mobile:** 2 (4.2% - Heavy Excel/PDF exports)
* **Backend Modification Required:** None! Existing production NestJS endpoints can be consumed without altering production code.
