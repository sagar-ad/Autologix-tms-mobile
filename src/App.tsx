import React, { useState } from 'react';
import { Header } from './components/Header';
import { ApiInventoryView } from './components/ApiInventoryView';
import { GapAnalysisView } from './components/GapAnalysisView';
import { SecurityView } from './components/SecurityView';
import { ArchitectureView } from './components/ArchitectureView';
import { MarkdownDocViewer } from './components/MarkdownDocViewer';
import { AndroidProjectView } from './components/AndroidProjectView';
import { API_INVENTORY } from './data/apiInventoryData';
import { DOCS_LIST } from './data/docsContent';
import {
  Layers,
  ShieldCheck,
  CheckCircle2,
  FileCode2,
  FolderGit2,
  Sparkles,
  AlertCircle,
  ExternalLink,
  ChevronRight
} from 'lucide-react';

export default function App() {
  const [activeTab, setActiveTab] = useState('android');

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-blue-500/30">
      {/* Top Navigation */}
      <Header
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        totalApisCount={API_INVENTORY.length}
      />

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
        {/* Governance Callout Banner */}
        <div className="bg-gradient-to-r from-blue-950/40 via-slate-900 to-slate-950 border border-blue-500/30 rounded-2xl p-5 shadow-lg shadow-blue-950/20">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div className="space-y-1.5">
              <div className="flex items-center gap-2">
                <span className="flex h-2.5 w-2.5 rounded-full bg-emerald-400"></span>
                <span className="text-xs uppercase font-bold tracking-wider text-emerald-400">
                  Architectural Feasibility & API Mapping: Complete
                </span>
              </div>
              <h2 className="text-lg font-bold text-white tracking-tight">
                AutoLogix TMS Android Solution Architecture & Readiness Assessment
              </h2>
              <p className="text-xs text-slate-400 max-w-3xl leading-relaxed">
                As Senior Android Solution Architect & Lead Developer, I have completed the comprehensive analysis of
                the existing NestJS backend and Angular frontend. All 5 documentation specifications have been generated
                in <code className="text-blue-400 font-mono">/docs</code>. No production code has been altered.
                Awaiting stakeholder approval before creating{' '}
                <code className="text-blue-400 font-mono">C:\repo\Autologix-TMS\mobile-app_v1</code>.
              </p>
            </div>

            <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2 shrink-0">
              <button
                id="view-api-btn"
                onClick={() => setActiveTab('inventory')}
                className="flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-500 text-white px-4 py-2 rounded-xl text-xs font-semibold shadow-sm transition-all"
              >
                Inspect 47 APIs
                <ChevronRight className="w-4 h-4" />
              </button>
              <button
                id="view-docs-btn"
                onClick={() => setActiveTab('docs')}
                className="flex items-center justify-center gap-2 bg-slate-800 hover:bg-slate-700 text-slate-200 px-4 py-2 rounded-xl text-xs font-semibold border border-slate-700 transition-all"
              >
                <FileCode2 className="w-4 h-4 text-blue-400" />
                Review 5 Docs
              </button>
            </div>
          </div>
        </div>

        {/* Dynamic View Swapper */}
        {activeTab === 'android' && <AndroidProjectView />}

        {activeTab === 'overview' && (
          <div className="space-y-6">
            {/* Quick Metrics KPI Bar */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
                <span className="text-slate-400 text-xs block">Documented API Endpoints</span>
                <div className="flex items-baseline gap-2 mt-1">
                  <span className="text-2xl font-bold text-white font-mono">{API_INVENTORY.length}</span>
                  <span className="text-xs text-emerald-400">across 32 categories</span>
                </div>
              </div>

              <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
                <span className="text-slate-400 text-xs block">Direct Android Reuse</span>
                <div className="flex items-baseline gap-2 mt-1">
                  <span className="text-2xl font-bold text-emerald-400 font-mono">89.4%</span>
                  <span className="text-xs text-slate-400">42 endpoints</span>
                </div>
              </div>

              <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
                <span className="text-slate-400 text-xs block">Backend Modification Required</span>
                <div className="flex items-baseline gap-2 mt-1">
                  <span className="text-2xl font-bold text-blue-400 font-mono">0</span>
                  <span className="text-xs text-slate-400">Zero backend changes</span>
                </div>
              </div>

              <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
                <span className="text-slate-400 text-xs block">Target Repository Location</span>
                <div className="mt-1">
                  <span className="text-xs text-blue-300 font-mono font-semibold block truncate">
                    mobile-app_v1
                  </span>
                  <span className="text-[10px] text-slate-500 font-mono truncate block">
                    C:\repo\Autologix-TMS
                  </span>
                </div>
              </div>
            </div>

            {/* Core Architectural Highlights */}
            <ArchitectureView />
          </div>
        )}

        {activeTab === 'inventory' && <ApiInventoryView apis={API_INVENTORY} />}

        {activeTab === 'gap' && <GapAnalysisView />}

        {activeTab === 'security' && <SecurityView />}

        {activeTab === 'docs' && <MarkdownDocViewer docs={DOCS_LIST} />}
      </main>

      {/* Footer */}
      <footer className="bg-slate-950 border-t border-slate-900 py-6 mt-12 text-slate-500 text-xs">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <span className="font-semibold text-slate-400">AutoLogix TMS Native Android Companion</span>
            <span>•</span>
            <span>Clean Architecture & MVVM Specification</span>
          </div>
          <div className="flex items-center gap-4 text-[11px] font-mono">
            <span>Status: Phase 0 Analysis Complete</span>
            <span>•</span>
            <span className="text-amber-400 font-medium">Awaiting Implementation Approval</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
