import React from 'react';
import { Cpu, Smartphone, Database, RefreshCw, Camera, Radio, FolderTree, Check } from 'lucide-react';
import { SPRINT_PHASES } from '../data/apiInventoryData';

export const ArchitectureView: React.FC = () => {
  return (
    <div className="space-y-6">
      {/* Architecture Highlights */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
        <h2 className="text-base font-bold text-white mb-1 flex items-center gap-2">
          <Cpu className="w-5 h-5 text-blue-400" />
          Native Android Clean Architecture & Hardware Integration
        </h2>
        <p className="text-xs text-slate-400 leading-relaxed max-w-4xl">
          Engineered for industrial shop-floor reliability in{' '}
          <code className="text-blue-400 font-mono">C:\repo\Autologix-TMS\mobile-app_v1</code>.
          Built using modern Kotlin 2.0, Jetpack Compose Material 3, Dagger Hilt, Retrofit 2, and CameraX + Google ML Kit.
        </p>
      </div>

      {/* Layer Architecture Breakdown */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {/* Presentation Layer */}
        <div className="bg-slate-900/90 border border-blue-500/30 rounded-xl p-5 space-y-3">
          <div className="flex items-center gap-2 text-blue-400 font-bold text-xs uppercase tracking-wider">
            <Smartphone className="w-4 h-4" />
            Presentation Layer
          </div>
          <h3 className="text-sm font-bold text-white">Jetpack Compose + Material 3</h3>
          <p className="text-xs text-slate-400 leading-relaxed">
            Single Activity architecture with Compose Navigation. MVI / Unidirectional Data Flow pattern.
          </p>
          <ul className="text-xs space-y-1.5 text-slate-300 font-mono text-[11px]">
            <li className="flex items-center gap-1.5">
              <Check className="w-3.5 h-3.5 text-blue-400 shrink-0" />
              StateFlow&lt;UiState&gt; lifecycle-aware
            </li>
            <li className="flex items-center gap-1.5">
              <Check className="w-3.5 h-3.5 text-blue-400 shrink-0" />
              SharedFlow&lt;UiEvent&gt; for one-shot alerts
            </li>
            <li className="flex items-center gap-1.5">
              <Check className="w-3.5 h-3.5 text-blue-400 shrink-0" />
              Trolley 360, Gate IN/OUT, PM Views
            </li>
          </ul>
        </div>

        {/* Domain Layer */}
        <div className="bg-slate-900/90 border border-purple-500/30 rounded-xl p-5 space-y-3">
          <div className="flex items-center gap-2 text-purple-400 font-bold text-xs uppercase tracking-wider">
            <Cpu className="w-4 h-4" />
            Domain Layer
          </div>
          <h3 className="text-sm font-bold text-white">Pure Kotlin Use Cases</h3>
          <p className="text-xs text-slate-400 leading-relaxed">
            Decoupled business use cases with zero Android dependencies. Direct unit-testability without mocks.
          </p>
          <ul className="text-xs space-y-1.5 text-slate-300 font-mono text-[11px]">
            <li className="flex items-center gap-1.5">
              <Check className="w-3.5 h-3.5 text-purple-400 shrink-0" />
              ScanTrolleyBarcodeUseCase
            </li>
            <li className="flex items-center gap-1.5">
              <Check className="w-3.5 h-3.5 text-purple-400 shrink-0" />
              ExecuteGateInOutUseCase
            </li>
            <li className="flex items-center gap-1.5">
              <Check className="w-3.5 h-3.5 text-purple-400 shrink-0" />
              SubmitPmChecklistUseCase
            </li>
          </ul>
        </div>

        {/* Data Layer */}
        <div className="bg-slate-900/90 border border-emerald-500/30 rounded-xl p-5 space-y-3">
          <div className="flex items-center gap-2 text-emerald-400 font-bold text-xs uppercase tracking-wider">
            <Database className="w-4 h-4" />
            Data Layer
          </div>
          <h3 className="text-sm font-bold text-white">Retrofit + Room SQLite</h3>
          <p className="text-xs text-slate-400 leading-relaxed">
            Repositories arbitrate between remote NestJS REST APIs and local encrypted offline cache.
          </p>
          <ul className="text-xs space-y-1.5 text-slate-300 font-mono text-[11px]">
            <li className="flex items-center gap-1.5">
              <Check className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
              TokenAuthenticator for auto refresh
            </li>
            <li className="flex items-center gap-1.5">
              <Check className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
              WorkManager background sync worker
            </li>
            <li className="flex items-center gap-1.5">
              <Check className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
              SQLCipher 256-bit encrypted DB
            </li>
          </ul>
        </div>
      </div>

      {/* Hardware Integrations Banner */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
        <h3 className="text-sm font-bold text-white mb-3 flex items-center gap-2">
          <Camera className="w-4 h-4 text-cyan-400" />
          Field Hardware Integration Highlights
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
          <div className="bg-slate-950 p-3.5 rounded-lg border border-slate-800 space-y-1">
            <span className="text-cyan-400 font-bold block">CameraX & Google ML Kit</span>
            <p className="text-slate-400">
              Continuous multi-format barcode/QR scanner (QR Code, Code 128, DataMatrix) with instant haptic vibration and sound chime on detection.
            </p>
          </div>

          <div className="bg-slate-950 p-3.5 rounded-lg border border-slate-800 space-y-1">
            <span className="text-cyan-400 font-bold block">Hardware Laser Intent Wedge</span>
            <p className="text-slate-400">
              BroadcastReceiver for Zebra DataWedge & Honeywell Enterprise scanners. Physical hardware scan buttons trigger direct text injection into ViewModels.
            </p>
          </div>

          <div className="bg-slate-950 p-3.5 rounded-lg border border-slate-800 space-y-1">
            <span className="text-cyan-400 font-bold block">Smart Image Compression</span>
            <p className="text-slate-400">
              Downsamples 12MP camera photos to 1600x1200 WebP/JPEG (&lt;500KB) on a background thread prior to multipart transmission to /media/upload.
            </p>
          </div>
        </div>
      </div>

      {/* Package Structure & Sprints */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {/* Package Structure Tree */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
          <h3 className="text-sm font-bold text-white mb-3 flex items-center gap-2">
            <FolderTree className="w-4 h-4 text-amber-400" />
            Kotlin Clean Architecture Package Hierarchy
          </h3>
          <pre className="bg-slate-950 border border-slate-800/80 p-3.5 rounded-lg text-slate-300 font-mono text-[11px] leading-relaxed overflow-x-auto">
{`com.autologix.tms/
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
    └── push/          # TmsFirebaseMessagingService`}
          </pre>
        </div>

        {/* Phase Breakdown */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm space-y-3">
          <h3 className="text-sm font-bold text-white flex items-center justify-between">
            <span className="flex items-center gap-2">
              <RefreshCw className="w-4 h-4 text-emerald-400" />
              7-Sprint Implementation Phasing
            </span>
            <span className="text-xs px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 font-mono">
              Awaiting Approval
            </span>
          </h3>

          <div className="space-y-2.5 max-h-[350px] overflow-y-auto pr-1">
            {SPRINT_PHASES.map((s) => (
              <div
                key={s.phase}
                className="bg-slate-950 border border-slate-800 rounded-lg p-3 text-xs space-y-1"
              >
                <div className="flex items-center justify-between">
                  <span className="font-bold text-slate-200">
                    Phase {s.phase}: {s.title}
                  </span>
                  <span className="text-[10px] font-mono text-slate-400">{s.duration}</span>
                </div>
                <p className="text-[11px] text-slate-400 line-clamp-1">
                  {s.deliverables[0]} and {s.deliverables.length - 1} more items.
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
