import React from 'react';
import { Shield, Key, Lock, CheckCircle2, UserCheck, Smartphone, Server, ArrowRight } from 'lucide-react';

export const SecurityView: React.FC = () => {
  const rbacMatrix = [
    { screen: 'Trolley 360 & Specs', roles: ['SA', 'OA', 'MM', 'MT', 'LO', 'VI'] },
    { screen: 'Gate IN / Gate OUT Scan', roles: ['SA', 'OA', 'LO (Primary)'] },
    { screen: 'Batch Dispatch Scan (40+ units)', roles: ['SA', 'OA', 'LO (Primary)'] },
    { screen: 'PM Schedule Calendar', roles: ['SA', 'OA', 'MM', 'MT (Primary)'] },
    { screen: 'Execute PM Checklist', roles: ['MM', 'MT (Primary)'] },
    { screen: 'Report Damage Incident', roles: ['SA', 'OA', 'MM', 'MT', 'LO'] },
    { screen: 'Approve Damage / Condemn Scrap', roles: ['SA', 'OA', 'MM (Primary)'] },
    { screen: 'Assign Work Orders to Techs', roles: ['SA', 'OA', 'MM (Primary)'] },
    { screen: 'Multi-Tenant Org Switcher', roles: ['Multi-assigned users only'] },
  ];

  return (
    <div className="space-y-6">
      {/* Security Architecture Header */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
        <h2 className="text-base font-bold text-white mb-1 flex items-center gap-2">
          <Shield className="w-5 h-5 text-emerald-400" />
          Enterprise Mobile Security & Multi-Organization Isolation
        </h2>
        <p className="text-xs text-slate-400 leading-relaxed max-w-4xl">
          AutoLogix TMS operates across OEM plants, tiered automotive parts suppliers, and shared logistics hubs.
          The Android companion architecture enforces a Zero-Trust client model: zero plaintext credentials on disk,
          hardware-backed cryptographic key storage, thread-safe token refreshing, and strict tenant separation.
        </p>
      </div>

      {/* Interactive Architecture Flow: Client to Backend */}
      <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-6 shadow-sm">
        <h3 className="text-sm font-bold text-white mb-4 flex items-center gap-2">
          <Lock className="w-4 h-4 text-blue-400" />
          Cryptographic Token & Tenant Header Flow
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
          {/* Step 1: Client Keystore */}
          <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 space-y-2">
            <div className="flex items-center gap-2 text-emerald-400 text-xs font-bold uppercase tracking-wider">
              <Key className="w-4 h-4" />
              1. Hardware Keystore (TEE)
            </div>
            <p className="text-xs text-slate-300 font-semibold">Jetpack Security MasterKey</p>
            <p className="text-[11px] text-slate-400 leading-relaxed">
              AES256-GCM encrypted tokens stored in EncryptedSharedPreferences. Keys never leave the device hardware security module.
            </p>
            <div className="bg-slate-900 p-2 rounded text-[10px] font-mono text-slate-300">
              • Access Token (15-min TTL)<br />
              • Refresh Token (7-day TTL)<br />
              • Active Organization ID
            </div>
          </div>

          {/* Arrow */}
          <div className="hidden md:flex flex-col items-center justify-center text-slate-500 gap-1">
            <span className="text-[11px] font-mono text-blue-400 text-center">TLS 1.3 Pinning</span>
            <div className="w-full h-0.5 bg-blue-500/40 relative">
              <ArrowRight className="w-4 h-4 text-blue-400 absolute right-0 -top-1.5" />
            </div>
            <span className="text-[10px] font-mono text-slate-400 text-center">x-organization-id</span>
          </div>

          {/* Step 2: NestJS Gateway Ingress */}
          <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 space-y-2">
            <div className="flex items-center gap-2 text-blue-400 text-xs font-bold uppercase tracking-wider">
              <Server className="w-4 h-4" />
              2. NestJS Tenant Guards
            </div>
            <p className="text-xs text-slate-300 font-semibold">Three-Tiered Gatekeeper</p>
            <p className="text-[11px] text-slate-400 leading-relaxed">
              Every request is cross-validated against token signature, user organization membership, and role permissions.
            </p>
            <div className="bg-slate-900 p-2 rounded text-[10px] font-mono text-slate-300">
              1. JwtAuthGuard (Valid signature)<br />
              2. TenantGuard (Cross-check orgId)<br />
              3. RolesGuard (Check RBAC)
            </div>
          </div>
        </div>
      </div>

      {/* RBAC Matrix Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
        <div className="p-4 border-b border-slate-800">
          <h3 className="text-sm font-bold text-white flex items-center gap-2">
            <UserCheck className="w-4 h-4 text-purple-400" />
            Role-Based Access Control (RBAC) Mobile Screen Matrix
          </h3>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-950 text-slate-400 uppercase font-semibold border-b border-slate-800">
              <tr>
                <th className="py-3 px-4">Mobile Screen / Feature</th>
                <th className="py-3 px-4">Super Admin</th>
                <th className="py-3 px-4">Org Admin</th>
                <th className="py-3 px-4">Maint. Manager</th>
                <th className="py-3 px-4">Maint. Tech</th>
                <th className="py-3 px-4">Logistics Operator</th>
                <th className="py-3 px-4">Viewer</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {rbacMatrix.map((item, idx) => {
                const hasRole = (roleStr: string) =>
                  item.roles.some((r) => r.toLowerCase().includes(roleStr.toLowerCase()));

                return (
                  <tr key={idx} className="hover:bg-slate-800/40">
                    <td className="py-3 px-4 font-medium text-slate-200">{item.screen}</td>
                    <td className="py-3 px-4">
                      {hasRole('SA') ? (
                        <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                      ) : (
                        <span className="text-slate-600">—</span>
                      )}
                    </td>
                    <td className="py-3 px-4">
                      {hasRole('OA') ? (
                        <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                      ) : (
                        <span className="text-slate-600">—</span>
                      )}
                    </td>
                    <td className="py-3 px-4">
                      {hasRole('MM') ? (
                        <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                      ) : (
                        <span className="text-slate-600">—</span>
                      )}
                    </td>
                    <td className="py-3 px-4">
                      {hasRole('MT') ? (
                        <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                      ) : (
                        <span className="text-slate-600">—</span>
                      )}
                    </td>
                    <td className="py-3 px-4">
                      {hasRole('LO') ? (
                        <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                      ) : (
                        <span className="text-slate-600">—</span>
                      )}
                    </td>
                    <td className="py-3 px-4">
                      {hasRole('VI') ? (
                        <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                      ) : (
                        <span className="text-slate-600">—</span>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
