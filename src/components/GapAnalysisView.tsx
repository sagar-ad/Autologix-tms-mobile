import React from 'react';
import { Ban, ShieldAlert, CheckSquare, UploadCloud, ListFilter, Sparkles, Layers } from 'lucide-react';
import { GAP_ANALYSIS_ITEMS } from '../data/apiInventoryData';

export const GapAnalysisView: React.FC = () => {
  const getCategoryIcon = (catName: string) => {
    switch (catName) {
      case 'APIs Missing for Mobile':
        return <Sparkles className="w-5 h-5 text-blue-400" />;
      case 'APIs Excluded from Mobile':
        return <Ban className="w-5 h-5 text-rose-400" />;
      case 'APIs Requiring Extra Authorization':
        return <ShieldAlert className="w-5 h-5 text-amber-400" />;
      case 'APIs Requiring Server Validation':
        return <CheckSquare className="w-5 h-5 text-emerald-400" />;
      case 'APIs Requiring File Upload & Pre-Compression':
        return <UploadCloud className="w-5 h-5 text-cyan-400" />;
      case 'APIs Requiring Pagination & DTO Trimming':
        return <ListFilter className="w-5 h-5 text-purple-400" />;
      default:
        return <Layers className="w-5 h-5 text-slate-400" />;
    }
  };

  const getBorderColor = (catName: string) => {
    switch (catName) {
      case 'APIs Missing for Mobile':
        return 'border-blue-500/30 bg-blue-950/20';
      case 'APIs Excluded from Mobile':
        return 'border-rose-500/30 bg-rose-950/20';
      case 'APIs Requiring Extra Authorization':
        return 'border-amber-500/30 bg-amber-950/20';
      case 'APIs Requiring Server Validation':
        return 'border-emerald-500/30 bg-emerald-950/20';
      case 'APIs Requiring File Upload & Pre-Compression':
        return 'border-cyan-500/30 bg-cyan-950/20';
      case 'APIs Requiring Pagination & DTO Trimming':
        return 'border-purple-500/30 bg-purple-950/20';
      default:
        return 'border-slate-800 bg-slate-900/60';
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Advisory Banner */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
        <h2 className="text-base font-bold text-white mb-1 flex items-center gap-2">
          <ShieldAlert className="w-5 h-5 text-amber-400" />
          Mobile API Gap Analysis & Field Architecture Gating
        </h2>
        <p className="text-xs text-slate-400 leading-relaxed max-w-4xl">
          Existing NestJS production APIs provide full enterprise coverage. Our field analysis evaluates
          battery life, intermittent dock Wi-Fi, rapid barcode bursts, and cellular data consumption.
          Below is the rigorous gating policy governing what mobile consumes, what it restricts, and how
          client-side optimizations preserve existing backend stability.
        </p>
      </div>

      {/* Grid of Gap Categories */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {GAP_ANALYSIS_ITEMS.map((group) => (
          <div
            key={group.category}
            className={`border rounded-xl p-5 shadow-sm space-y-4 ${getBorderColor(group.category)}`}
          >
            <div className="flex items-center justify-between border-b border-slate-800/80 pb-3">
              <div className="flex items-center gap-2.5">
                {getCategoryIcon(group.category)}
                <h3 className="text-sm font-bold text-white tracking-tight">{group.category}</h3>
              </div>
              <span className="text-xs font-mono px-2 py-0.5 rounded-full bg-slate-800 text-slate-300">
                {group.count} Items
              </span>
            </div>

            <div className="space-y-3">
              {group.items.map((item, idx) => (
                <div
                  key={idx}
                  className="bg-slate-950/70 border border-slate-800/80 rounded-lg p-3.5 space-y-1.5"
                >
                  <h4 className="text-xs font-semibold text-slate-200">{item.title}</h4>
                  <p className="text-xs text-slate-400 leading-normal">{item.desc}</p>
                  <div className="pt-1.5 border-t border-slate-900 flex items-start gap-1.5 text-[11px] text-blue-300">
                    <span className="font-semibold text-slate-400">Architect Strategy:</span>
                    <span>{item.solution}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
