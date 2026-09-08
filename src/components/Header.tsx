import React from 'react';
import { Layers, Smartphone, ShieldCheck, FileText, CheckCircle2, Clock } from 'lucide-react';

interface HeaderProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  totalApisCount: number;
}

export const Header: React.FC<HeaderProps> = ({ activeTab, setActiveTab, totalApisCount }) => {
  const tabs = [
    { id: 'android', label: 'Android App (mobile-app_v1)', icon: Smartphone },
    { id: 'overview', label: 'Architecture Overview', icon: Layers },
    { id: 'inventory', label: `API Inventory (${totalApisCount})`, icon: Smartphone },
    { id: 'gap', label: 'Gap Analysis & Gating', icon: CheckCircle2 },
    { id: 'security', label: 'Security & Multi-Tenancy', icon: ShieldCheck },
    { id: 'docs', label: 'Generated Documentation (5 Files)', icon: FileText },
  ];

  return (
    <header className="bg-slate-900 border-b border-slate-800 text-white sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between py-4 gap-4">
          <div>
            <div className="flex items-center gap-3">
              <div className="h-9 w-9 rounded-lg bg-blue-600 flex items-center justify-center font-bold text-white shadow-md shadow-blue-500/20">
                AX
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h1 className="text-xl font-bold tracking-tight text-white">AutoLogix TMS</h1>
                  <span className="text-xs px-2 py-0.5 rounded-full bg-blue-500/20 text-blue-300 font-medium border border-blue-500/30">
                    Android Companion
                  </span>
                </div>
                <p className="text-xs text-slate-400">
                  Target: <code className="text-blue-400 font-mono">C:\repo\Autologix-TMS\mobile-app_v1</code>
                </p>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2 bg-amber-500/10 border border-amber-500/30 px-3 py-1.5 rounded-lg text-amber-300 text-xs">
              <Clock className="w-3.5 h-3.5 text-amber-400 animate-pulse" />
              <span className="font-semibold">Governance Gate:</span> Analysis Complete — Awaiting Implementation Approval
            </div>
          </div>
        </div>

        {/* Navigation Tabs */}
        <nav className="flex space-x-1 overflow-x-auto border-t border-slate-800/80 pt-1 pb-1 scrollbar-none">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                id={`tab-btn-${tab.id}`}
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center gap-2 px-3 py-2 text-sm font-medium rounded-md whitespace-nowrap transition-colors ${
                  isActive
                    ? 'bg-blue-600/20 text-blue-300 border border-blue-500/40'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                }`}
              >
                <Icon className="w-4 h-4" />
                {tab.label}
              </button>
            );
          })}
        </nav>
      </div>
    </header>
  );
};
