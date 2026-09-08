export interface ApiEndpointItem {
  id: string;
  categoryNumber: number;
  categoryName: string;
  method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  endpoint: string;
  requestModel: string;
  responseModel: string;
  auth: 'Public' | 'Bearer' | 'Refresh';
  roles: string[];
  orgRequirement: 'Header' | 'JWT' | 'None';
  pagination: 'Yes' | 'No' | 'Cursor';
  mobileSuitability: 'Direct' | 'Optimizable' | 'Restricted';
  existingAngularUsage: string;
  action: 'Reuse Direct' | 'Extend/Wrap' | 'Do Not Expose';
  description: string;
  offlineSupport?: boolean;
}

export interface GapAnalysisItem {
  id: string;
  title: string;
  category: 'MISSING_API' | 'NOT_EXPOSED' | 'EXTRA_AUTH' | 'STRICT_VALIDATION' | 'FILE_UPLOAD' | 'PAGINATION' | 'OPTIMIZED_RESPONSE';
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  description: string;
  recommendation: string;
  backendImpact: string;
}

export interface SecurityLayer {
  title: string;
  component: string;
  standard: string;
  description: string;
}

export interface SprintPhase {
  phaseNumber: number;
  phaseTitle: string;
  duration: string;
  focus: string;
  deliverables: string[];
  status: 'PENDING_APPROVAL' | 'PLANNED' | 'IN_PROGRESS' | 'DONE';
}

export interface DocFile {
  name: string;
  path: string;
  title: string;
  description: string;
  content: string;
}
