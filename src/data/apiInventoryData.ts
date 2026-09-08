import { ApiEndpointItem } from '../types';

export const API_INVENTORY: ApiEndpointItem[] = [
  // 1. Existing authentication APIs
  {
    id: 'api-1',
    categoryNumber: 1,
    categoryName: 'Authentication',
    method: 'POST',
    endpoint: '/api/v1/auth/login',
    requestModel: 'LoginDto { username, password, deviceId?, devicePlatform? }',
    responseModel: 'AuthResponseDto { accessToken, refreshToken, expiresIn, user, organizations[] }',
    auth: 'Public',
    roles: ['All'],
    orgRequirement: 'None',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Primary login view in auth.service.ts',
    action: 'Reuse Direct',
    description: 'Authenticates credentials, returns dual JWT tokens, user profile and tenant list.',
    offlineSupport: false
  },
  // 2. Token mechanism & Refresh token
  {
    id: 'api-2',
    categoryNumber: 2,
    categoryName: 'Token Mechanism',
    method: 'POST',
    endpoint: '/api/v1/auth/refresh',
    requestModel: 'RefreshTokenDto { refreshToken }',
    responseModel: 'TokenPairDto { accessToken, refreshToken, expiresIn }',
    auth: 'Refresh',
    roles: ['All'],
    orgRequirement: 'None',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Angular HTTP interceptor for silent 401 refresh',
    action: 'Reuse Direct',
    description: 'Exchanges valid refresh token for fresh short-lived access token.',
    offlineSupport: false
  },
  {
    id: 'api-3',
    categoryNumber: 2,
    categoryName: 'Session Termination',
    method: 'POST',
    endpoint: '/api/v1/auth/logout',
    requestModel: 'LogoutDto { refreshToken, deviceId? }',
    responseModel: 'MessageResponseDto { success: boolean, message: string }',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'None',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Navbar logout action',
    action: 'Reuse Direct',
    description: 'Invalidates active refresh token server-side and clears device binding.',
    offlineSupport: false
  },
  // 3. Password recovery
  {
    id: 'api-4',
    categoryNumber: 3,
    categoryName: 'Password Recovery',
    method: 'POST',
    endpoint: '/api/v1/auth/forgot-password',
    requestModel: 'ForgotPasswordDto { email }',
    responseModel: 'MessageResponseDto { success: boolean, message: string }',
    auth: 'Public',
    roles: ['All'],
    orgRequirement: 'None',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Forgot password modal dialog',
    action: 'Reuse Direct',
    description: 'Dispatches password reset link and OTP token via email.',
    offlineSupport: false
  },
  // 5. User profile API
  {
    id: 'api-5',
    categoryNumber: 5,
    categoryName: 'User Profile',
    method: 'GET',
    endpoint: '/api/v1/users/me',
    requestModel: 'None',
    responseModel: 'UserProfileDto { id, firstName, lastName, email, phone, avatarUrl, roles[], currentOrgId }',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'JWT',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Bootstrap user context on app boot',
    action: 'Reuse Direct',
    description: 'Retrieves current authenticated profile and assigned privileges.',
    offlineSupport: true
  },
  {
    id: 'api-6',
    categoryNumber: 5,
    categoryName: 'User Profile',
    method: 'PATCH',
    endpoint: '/api/v1/users/me/profile',
    requestModel: 'UpdateProfileDto { firstName, lastName, phone, avatarUrl? }',
    responseModel: 'UserProfileDto',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'JWT',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Account settings page',
    action: 'Reuse Direct',
    description: 'Updates personal profile metadata and contact information.',
    offlineSupport: false
  },
  // 6. Organization API
  {
    id: 'api-7',
    categoryNumber: 6,
    categoryName: 'Organization',
    method: 'GET',
    endpoint: '/api/v1/organizations',
    requestModel: 'Query: ?status=ACTIVE',
    responseModel: 'OrganizationListDto[] { id, name, code, logoUrl, status, trolleyPrefix }',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'None',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Multi-organization selector dropdown',
    action: 'Reuse Direct',
    description: 'Returns tenant organizations accessible by current user credentials.',
    offlineSupport: true
  },
  {
    id: 'api-8',
    categoryNumber: 6,
    categoryName: 'Organization',
    method: 'POST',
    endpoint: '/api/v1/organizations/switch',
    requestModel: 'SwitchOrgDto { organizationId: string }',
    responseModel: 'SwitchOrgResponseDto { scopedAccessToken, currentOrg }',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'None',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Tenant switch in top desktop navbar',
    action: 'Reuse Direct',
    description: 'Switches tenant context and provides scoped JWT token.',
    offlineSupport: false
  },
  // 7. Role API
  {
    id: 'api-9',
    categoryNumber: 7,
    categoryName: 'Role & Permissions',
    method: 'GET',
    endpoint: '/api/v1/roles/permissions',
    requestModel: 'None',
    responseModel: 'RolePermissionsDto { role, permissions: string[] }',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Angular route guards and dynamic feature toggles',
    action: 'Reuse Direct',
    description: 'Retrieves granular permission set for current tenant role.',
    offlineSupport: true
  },
  // 8. Customer APIs
  {
    id: 'api-10',
    categoryNumber: 8,
    categoryName: 'Customer Management',
    method: 'GET',
    endpoint: '/api/v1/customers',
    requestModel: 'Query: { page, limit, search, status }',
    responseModel: 'PaginatedResponse<CustomerDto> { items, total, page, totalPages }',
    auth: 'Bearer',
    roles: ['SA', 'OA', 'MM', 'LO'],
    orgRequirement: 'Header',
    pagination: 'Yes',
    mobileSuitability: 'Optimizable',
    existingAngularUsage: 'Customer master management grid',
    action: 'Reuse Direct',
    description: 'List customer manufacturing partners, delivery plants, and contacts.',
    offlineSupport: true
  },
  // 9. Project APIs
  {
    id: 'api-11',
    categoryNumber: 9,
    categoryName: 'Project Master',
    method: 'GET',
    endpoint: '/api/v1/projects',
    requestModel: 'Query: { customerId?, status=ACTIVE }',
    responseModel: 'PaginatedResponse<ProjectDto>',
    auth: 'Bearer',
    roles: ['SA', 'OA', 'MM', 'LO'],
    orgRequirement: 'Header',
    pagination: 'Yes',
    mobileSuitability: 'Optimizable',
    existingAngularUsage: 'Project allocation dropdowns & views',
    action: 'Reuse Direct',
    description: 'Automotive vehicle projects (e.g. SUV Frame, Sedan Door Panel assembly).',
    offlineSupport: true
  },
  // 10 - 13. Trolley, 360 & Barcode APIs
  {
    id: 'api-12',
    categoryNumber: 10,
    categoryName: 'Trolley Fleet',
    method: 'GET',
    endpoint: '/api/v1/trolleys',
    requestModel: 'Query: { page, limit, search, status, type, locationId }',
    responseModel: 'PaginatedResponse<TrolleySummaryDto>',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'Header',
    pagination: 'Yes',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Main trolley inventory table',
    action: 'Reuse Direct',
    description: 'Paginated listing of all organization trolleys with live location status.',
    offlineSupport: true
  },
  {
    id: 'api-13',
    categoryNumber: 11,
    categoryName: 'Trolley Details',
    method: 'GET',
    endpoint: '/api/v1/trolleys/:id',
    requestModel: 'Path: id (UUID)',
    responseModel: 'TrolleyFullDetailDto { id, serialNo, type, status, location, specs, customer }',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Optimizable',
    existingAngularUsage: 'Trolley details drawer view',
    action: 'Reuse Direct',
    description: 'Single trolley comprehensive specification, tare weight, dimensions, and parts.',
    offlineSupport: true
  },
  {
    id: 'api-14',
    categoryNumber: 12,
    categoryName: 'Trolley 360 Lifecycle',
    method: 'GET',
    endpoint: '/api/v1/trolleys/:id/360',
    requestModel: 'Path: id (UUID)',
    responseModel: 'Trolley360ViewDto { specs, location, activeDamage, pmHistory, movementHistory }',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Angular 360 interactive timeline visualizer',
    action: 'Reuse Direct',
    description: 'Unified 360-degree timeline containing specs, damages, PM audits, and dispatch logs.',
    offlineSupport: false
  },
  {
    id: 'api-15',
    categoryNumber: 13,
    categoryName: 'Trolley QR / Barcode Scan',
    method: 'GET',
    endpoint: '/api/v1/trolleys/scan/:barcode',
    requestModel: 'Path: barcode (QR code string / linear barcode value)',
    responseModel: 'TrolleyQuickScanDto { id, serialNo, qrCode, type, status, currentLocation, isDamaged }',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Manual search bar barcode resolution',
    action: 'Reuse Direct',
    description: 'Instant resolution of scanned optical codes (QR/Code128/DataMatrix) to trolley entity.',
    offlineSupport: true
  },
  {
    id: 'api-16',
    categoryNumber: 13,
    categoryName: 'Trolley Registration',
    method: 'POST',
    endpoint: '/api/v1/trolleys/register',
    requestModel: 'CreateTrolleyDto { serialNo, qrCode, typeId, projectId, tareWeight, specs }',
    responseModel: 'TrolleySummaryDto',
    auth: 'Bearer',
    roles: ['SA', 'OA', 'MM'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Add new trolley modal',
    action: 'Reuse Direct',
    description: 'Enrolls new physical trolley into fleet with initial calibration data.',
    offlineSupport: false
  },
  // 14 - 16. Preventive Maintenance (PM)
  {
    id: 'api-17',
    categoryNumber: 14,
    categoryName: 'PM Schedules',
    method: 'GET',
    endpoint: '/api/v1/pm/schedules',
    requestModel: 'Query: { technicianId?, status, dateFrom, dateTo, page, limit }',
    responseModel: 'PaginatedResponse<PmScheduleDto>',
    auth: 'Bearer',
    roles: ['SA', 'OA', 'MM', 'MT'],
    orgRequirement: 'Header',
    pagination: 'Yes',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Maintenance calendar schedule view',
    action: 'Reuse Direct',
    description: 'Lists upcoming, due, and overdue preventive maintenance schedules.',
    offlineSupport: true
  },
  {
    id: 'api-18',
    categoryNumber: 15,
    categoryName: 'PM Checklist Templates',
    method: 'GET',
    endpoint: '/api/v1/pm/checklists/templates/:typeId',
    requestModel: 'Path: typeId (Trolley Type UUID)',
    responseModel: 'ChecklistTemplateDto { sections[], items[]: { title, criteria, isMandatory, requiresPhoto } }',
    auth: 'Bearer',
    roles: ['SA', 'OA', 'MM', 'MT'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'PM audit checklist configuration view',
    action: 'Reuse Direct',
    description: 'Retrieves mandatory inspection criteria (bearings, welds, locks, rubber stoppers).',
    offlineSupport: true
  },
  {
    id: 'api-19',
    categoryNumber: 16,
    categoryName: 'PM Execution',
    method: 'POST',
    endpoint: '/api/v1/pm/executions',
    requestModel: 'CreatePmExecutionDto { scheduleId, trolleyId, itemResults[], overallStatus, signatureUrl? }',
    responseModel: 'PmExecutionResultDto { id, status, completedAt }',
    auth: 'Bearer',
    roles: ['MM', 'MT'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Technician PM submission dialog',
    action: 'Reuse Direct',
    description: 'Submits completed physical checklist with item evaluations and digital sign-off.',
    offlineSupport: true
  },
  // 17 - 18. Customer Damage & Incidents
  {
    id: 'api-20',
    categoryNumber: 17,
    categoryName: 'Damage Reporting',
    method: 'POST',
    endpoint: '/api/v1/damages',
    requestModel: 'ReportDamageDto { trolleyId, severity, category, description, photoUrls[], reportedLocationId }',
    responseModel: 'DamageReportDto { id, ticketNo, status: OPEN, createdAt }',
    auth: 'Bearer',
    roles: ['SA', 'OA', 'MM', 'MT', 'LO'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Report incident wizard',
    action: 'Reuse Direct',
    description: 'Logs physical damage ticket with severity level and photographic evidence.',
    offlineSupport: true
  },
  {
    id: 'api-21',
    categoryNumber: 18,
    categoryName: 'Damage Tickets Feed',
    method: 'GET',
    endpoint: '/api/v1/damages',
    requestModel: 'Query: { status, severity, trolleyId?, page, limit }',
    responseModel: 'PaginatedResponse<DamageReportSummaryDto>',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'Header',
    pagination: 'Yes',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Damage tickets table',
    action: 'Reuse Direct',
    description: 'Filtered list of active and historical damage incidents.',
    offlineSupport: true
  },
  {
    id: 'api-22',
    categoryNumber: 18,
    categoryName: 'Damage Review & Status',
    method: 'PATCH',
    endpoint: '/api/v1/damages/:id/status',
    requestModel: 'UpdateDamageStatusDto { status: IN_REVIEW | APPROVED | REJECTED | SCRAP, comment }',
    responseModel: 'DamageReportDto',
    auth: 'Bearer',
    roles: ['SA', 'OA', 'MM'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Manager review approval buttons',
    action: 'Reuse Direct',
    description: 'Approves repair work order or condemns irreparable trolley to scrap.',
    offlineSupport: false
  },
  // 19 - 21. Assignments & Maintenance Work Orders
  {
    id: 'api-23',
    categoryNumber: 19,
    categoryName: 'Work Order Assignment',
    method: 'POST',
    endpoint: '/api/v1/maintenance/work-orders',
    requestModel: 'CreateWorkOrderDto { damageId?, pmScheduleId?, trolleyId, assignedTechnicianId, priority, targetDate }',
    responseModel: 'WorkOrderDto',
    auth: 'Bearer',
    roles: ['SA', 'OA', 'MM'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Assign work order modal',
    action: 'Reuse Direct',
    description: 'Creates repair or maintenance work order and assigns it to a technician.',
    offlineSupport: false
  },
  {
    id: 'api-24',
    categoryNumber: 20,
    categoryName: 'Technician Work Orders',
    method: 'GET',
    endpoint: '/api/v1/maintenance/work-orders/my',
    requestModel: 'Query: { status, page, limit }',
    responseModel: 'PaginatedResponse<WorkOrderSummaryDto>',
    auth: 'Bearer',
    roles: ['MT'],
    orgRequirement: 'Header',
    pagination: 'Yes',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Technician personal workbench',
    action: 'Reuse Direct',
    description: 'Active work orders assigned directly to the authenticated technician.',
    offlineSupport: true
  },
  {
    id: 'api-25',
    categoryNumber: 21,
    categoryName: 'Work Order Action',
    method: 'PATCH',
    endpoint: '/api/v1/maintenance/work-orders/:id/action',
    requestModel: 'WorkOrderActionDto { action: START | PAUSE | COMPLETE, technicianNotes, partsReplaced[], photos[] }',
    responseModel: 'WorkOrderDto',
    auth: 'Bearer',
    roles: ['MT', 'MM'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Technician action buttons (Start/Complete repair)',
    action: 'Reuse Direct',
    description: 'Records time-tracking, spare parts utilized, and repair completion.',
    offlineSupport: true
  },
  // 22 - 23. Logistics & Trolley IN/OUT
  {
    id: 'api-26',
    categoryNumber: 22,
    categoryName: 'Logistics Gate IN',
    method: 'POST',
    endpoint: '/api/v1/logistics/movement/gate-in',
    requestModel: 'GateInDto { trolleyId, gatePassNumber, sourceLocationId, truckNumber, driverName, conditionStatus, remarks, photos[]? }',
    responseModel: 'MovementLogDto { id, movementType: IN, timestamp }',
    auth: 'Bearer',
    roles: ['SA', 'OA', 'LO'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Dock receiving gate form',
    action: 'Reuse Direct',
    description: 'Records trolley arrival at facility gate with transport details and condition.',
    offlineSupport: true
  },
  {
    id: 'api-27',
    categoryNumber: 23,
    categoryName: 'Logistics Gate OUT',
    method: 'POST',
    endpoint: '/api/v1/logistics/movement/gate-out',
    requestModel: 'GateOutDto { trolleyId, dispatchNumber, destinationLocationId, truckNumber, driverName, sealNumber? }',
    responseModel: 'MovementLogDto { id, movementType: OUT, timestamp }',
    auth: 'Bearer',
    roles: ['SA', 'OA', 'LO'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Dock dispatch gate form',
    action: 'Reuse Direct',
    description: 'Dispatches trolley to supplier, customer assembly line, or satellite warehouse.',
    offlineSupport: true
  },
  {
    id: 'api-28',
    categoryNumber: 23,
    categoryName: 'Batch Gate Movement',
    method: 'POST',
    endpoint: '/api/v1/logistics/movement/batch',
    requestModel: 'BatchMovementDto { type: IN | OUT, trolleyIds: string[], manifestNo, truckNumber, destinationLocationId? }',
    responseModel: 'BatchMovementResultDto { succeeded: string[], failed: { trolleyId, reason }[] }',
    auth: 'Bearer',
    roles: ['SA', 'OA', 'LO'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Bulk dispatch wizard',
    action: 'Reuse Direct',
    description: 'Atomic batch processing for 10-60 scanned trolleys on a single truckload.',
    offlineSupport: true
  },
  // 24 - 25. Dashboard & Reports
  {
    id: 'api-29',
    categoryNumber: 24,
    categoryName: 'Dashboard KPIs',
    method: 'GET',
    endpoint: '/api/v1/dashboard/kpis',
    requestModel: 'Query: { timeRange? }',
    responseModel: 'DashboardKpiDto { totalTrolleys, inYardCount, inTransitCount, underMaintenanceCount, criticalDamagesCount, pmComplianceRate }',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Top summary cards on main dashboard',
    action: 'Reuse Direct',
    description: 'High-level real-time operations counts and fleet health metrics.',
    offlineSupport: true
  },
  {
    id: 'api-30',
    categoryNumber: 25,
    categoryName: 'Heavy Report Exports',
    method: 'GET',
    endpoint: '/api/v1/reports/trolley-movement/export',
    requestModel: 'Query: { dateFrom, dateTo, format=EXCEL | PDF }',
    responseModel: 'Binary Stream (Excel/PDF)',
    auth: 'Bearer',
    roles: ['SA', 'OA', 'MM'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Restricted',
    existingAngularUsage: 'Export button in reports tab',
    action: 'Do Not Expose',
    description: 'Large file report generation; not suitable for mobile memory/cellular data.',
    offlineSupport: false
  },
  // 26 - 29. Notifications, Upload & Search
  {
    id: 'api-31',
    categoryNumber: 26,
    categoryName: 'Notifications Feed',
    method: 'GET',
    endpoint: '/api/v1/notifications',
    requestModel: 'Query: { unreadOnly=true, page, limit }',
    responseModel: 'PaginatedResponse<NotificationItemDto>',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'Header',
    pagination: 'Yes',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Notification popover bell',
    action: 'Reuse Direct',
    description: 'User notification inbox for assignment alerts, PM reminders, and system events.',
    offlineSupport: true
  },
  {
    id: 'api-32',
    categoryNumber: 27,
    categoryName: 'Media Upload',
    method: 'POST',
    endpoint: '/api/v1/media/upload',
    requestModel: 'multipart/form-data { file: Binary, tag: DAMAGE | PM | GATE }',
    responseModel: 'MediaUploadResponseDto { url, fileKey, mimeType, size }',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'File drag-and-drop dropzone',
    action: 'Reuse Direct',
    description: 'Accepts compressed camera photos and returns persistent CDN storage URLs.',
    offlineSupport: true
  },
  {
    id: 'api-33',
    categoryNumber: 28,
    categoryName: 'Global Search',
    method: 'GET',
    endpoint: '/api/v1/search/global',
    requestModel: 'Query: { q: string, types?: string[] }',
    responseModel: 'GlobalSearchResultDto { trolleys[], workOrders[], damages[] }',
    auth: 'Bearer',
    roles: ['All'],
    orgRequirement: 'Header',
    pagination: 'No',
    mobileSuitability: 'Direct',
    existingAngularUsage: 'Global search bar in desktop header',
    action: 'Reuse Direct',
    description: 'Fast cross-entity autocomplete search across serial numbers, tickets, and work orders.',
    offlineSupport: false
  }
];

export const GAP_ANALYSIS_ITEMS: {
  category: string;
  count: number;
  items: { title: string; desc: string; solution: string }[];
}[] = [
  {
    category: 'APIs Missing for Mobile',
    count: 3,
    items: [
      {
        title: 'FCM Device Token Registration',
        desc: 'Android devices need to register their Firebase Cloud Messaging tokens to receive push notifications for critical work orders.',
        solution: 'Add POST /api/v1/devices/register (additive, does not break any existing APIs).'
      },
      {
        title: 'Batch Barcode Scan Resolver',
        desc: 'Scanning 40 trolleys sequentially benefits from a bulk verification endpoint instead of 40 individual HTTP calls.',
        solution: 'Android client maintains local accumulator queue and uses POST /logistics/movement/batch with zero backend changes.'
      },
      {
        title: 'Timestamp-based Delta Sync',
        desc: 'Technicians working in dead-zones need changes since last sync timestamp.',
        solution: 'Client implements Room local database with WorkManager background synchronization.'
      }
    ]
  },
  {
    category: 'APIs Excluded from Mobile',
    count: 4,
    items: [
      {
        title: 'Super-Admin Tenant Provisioning',
        desc: 'POST /admin/tenants',
        solution: 'Restricted strictly to web portal for IT governance.'
      },
      {
        title: 'Heavy Excel/PDF Reports Export',
        desc: 'GET /reports/*/export',
        solution: 'Massive binary downloads excluded from mobile; users view on-screen KPI dashboards instead.'
      },
      {
        title: 'Database Schema Migrations & Seeding',
        desc: 'POST /admin/database/seed',
        solution: 'Security policy forbids mobile access.'
      },
      {
        title: 'Bulk CSV Fleet Ingestion',
        desc: 'POST /trolleys/bulk-import-csv',
        solution: 'Desktop spreadsheet workflow reserved for web portal.'
      }
    ]
  },
  {
    category: 'APIs Requiring Extra Authorization',
    count: 2,
    items: [
      {
        title: 'Trolley Scrap Condemnation',
        desc: 'Marking a trolley as SCRAP removes it from operational circulation.',
        solution: 'Enforced via Maintenance Manager role check + supervisor PIN/Biometric confirmation prompt.'
      },
      {
        title: 'Gate Status Mismatch Override',
        desc: 'Gate-IN for a trolley not marked as OUT in system records.',
        solution: 'Requires mandatory supervisor override comment audited with user ID.'
      }
    ]
  },
  {
    category: 'APIs Requiring Server Validation',
    count: 3,
    items: [
      {
        title: 'Gate Movement State Machine',
        desc: 'Validating that a trolley is in the correct state before IN/OUT.',
        solution: 'NestJS ValidationPipe ensures no double Gate-IN or dispatching a trolley that is under active repair.'
      },
      {
        title: 'PM Checklist Mandatory Fields',
        desc: 'Safety critical checklist items must not be submitted blank.',
        solution: 'NestJS verifies all mandatory inspection items have valid boolean pass/fail flags.'
      },
      {
        title: 'Critical Damage Photo Requirement',
        desc: 'Critical severity tickets require photographic proof.',
        solution: 'Server validation ensures photoUrls array is not empty when severity == CRITICAL.'
      }
    ]
  },
  {
    category: 'APIs Requiring File Upload & Pre-Compression',
    count: 1,
    items: [
      {
        title: 'Camera Photo Optimization Pipeline',
        desc: 'Raw phone photos are 6-12MB. Uploading multiple photos over factory LTE can cause timeouts.',
        solution: 'Android client downsamples images to 1600x1200 WebP/JPEG (<500KB) before calling POST /media/upload.'
      }
    ]
  },
  {
    category: 'APIs Requiring Pagination & DTO Trimming',
    count: 5,
    items: [
      {
        title: 'Trolley Catalog Pagination',
        desc: 'Large manufacturing plants have 5,000+ trolleys.',
        solution: 'Android Jetpack Paging 3 configured with page size 25.'
      },
      {
        title: 'Trolley 360 Response Trimming',
        desc: 'Angular 360 loads massive nested relation tree.',
        solution: 'Client requests lean summary or parses only the latest 5 movements.'
      }
    ]
  }
];

export const SPRINT_PHASES: {
  phase: number;
  title: string;
  duration: string;
  deliverables: string[];
  status: 'AWAITING_APPROVAL' | 'PLANNED';
}[] = [
  {
    phase: 0,
    title: 'Project Scaffolding & Architecture Skeleton',
    duration: 'Sprint 1 (Week 1-2)',
    status: 'AWAITING_APPROVAL',
    deliverables: [
      'Initialize Gradle Version Catalog (libs.versions.toml) with Kotlin 2.0 & Compose BOM',
      'Clean Architecture multi-layer package structure in C:\\repo\\Autologix-TMS\\mobile-app_v1',
      'Retrofit + OkHttp network client with AuthInterceptor and TokenAuthenticator',
      'Android Keystore MasterKey & EncryptedSharedPreferences wrapper',
      'Static analysis: Detekt, ktlint, and Android Lint'
    ]
  },
  {
    phase: 1,
    title: 'Authentication & Multi-Tenant Context',
    duration: 'Sprint 2 (Week 3-4)',
    status: 'AWAITING_APPROVAL',
    deliverables: [
      'Material 3 Login screen with credential validation',
      'Organization selection & switching screen with scoped JWT token management',
      'Silent token refresh handling HTTP 401 via OkHttp Authenticator',
      'Biometric authentication prompt (BiometricPrompt) for shift quick-unlock',
      'Role-based navigation graph (Logistics vs. Technician vs. Manager views)'
    ]
  },
  {
    phase: 2,
    title: 'Optical Barcode/QR Scanning & Trolley 360',
    duration: 'Sprint 3 (Week 5-6)',
    status: 'AWAITING_APPROVAL',
    deliverables: [
      'CameraX integration with Google ML Kit Barcode Scanning (QR, Code128, DataMatrix)',
      'Hardware intent wedge BroadcastReceiver for Zebra & Honeywell enterprise scanners',
      'Scan resolver calling GET /trolleys/scan/:barcode',
      'Trolley 360 screen with live status, location badge, and specs',
      'Trolley catalog with Jetpack Paging 3 and status filter chips'
    ]
  },
  {
    phase: 3,
    title: 'Logistics Operations & Gate IN/OUT Scanning',
    duration: 'Sprint 4 (Week 7-8)',
    status: 'AWAITING_APPROVAL',
    deliverables: [
      'Gate IN screen with truck number, gate pass, and condition assessment',
      'Gate OUT screen with destination plant and dispatch challan',
      'Continuous batch scan accumulator mode for 40+ trolleys',
      'Atomic batch submission to POST /logistics/movement/batch',
      'Room database offline queue for gate transactions during Wi-Fi drops'
    ]
  },
  {
    phase: 4,
    title: 'Preventive Maintenance & Damage Management',
    duration: 'Sprint 5 (Week 9-10)',
    status: 'AWAITING_APPROVAL',
    deliverables: [
      'Technician workbench: "My Work Orders" list with priority badges',
      'Dynamic PM Checklist runner with pass/fail toggles and digital signature canvas',
      'Damage reporting with in-app camera capture and automatic image compression',
      'Multipart upload to POST /media/upload with progress indicator',
      'Manager damage review & work order assignment screens'
    ]
  },
  {
    phase: 5,
    title: 'Notifications, WorkManager Sync & Hardening',
    duration: 'Sprint 6 (Week 11-12)',
    status: 'AWAITING_APPROVAL',
    deliverables: [
      'Firebase Cloud Messaging (FCM) integration for urgent work order alerts',
      'Android WorkManager SyncWorker for automatic background offline queue flush',
      'Executive Dashboard with KPIs and fleet health metrics',
      'Certificate Pinning (network_security_config.xml) & Play Integrity checks',
      'Enterprise MDM build flavors and signed release bundles'
    ]
  }
];
