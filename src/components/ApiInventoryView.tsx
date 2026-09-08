import React, { useState, useMemo } from 'react';
import { Search, Filter, CheckCircle, AlertTriangle, XCircle, Code, Shield, Building2, ArrowUpRight, X } from 'lucide-react';
import { ApiEndpointItem } from '../types';

interface ApiInventoryViewProps {
  apis: ApiEndpointItem[];
}

export const ApiInventoryView: React.FC<ApiInventoryViewProps> = ({ apis }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedMethod, setSelectedMethod] = useState<string>('ALL');
  const [selectedSuitability, setSelectedSuitability] = useState<string>('ALL');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [activeModalItem, setActiveModalItem] = useState<ApiEndpointItem | null>(null);

  const categories = useMemo(() => {
    const set = new Set<string>();
    apis.forEach((a) => set.add(a.categoryName));
    return ['ALL', ...Array.from(set)];
  }, [apis]);

  const filteredApis = useMemo(() => {
    return apis.filter((item) => {
      const matchesSearch =
        item.endpoint.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.categoryName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.existingAngularUsage.toLowerCase().includes(searchTerm.toLowerCase());

      const matchesMethod = selectedMethod === 'ALL' || item.method === selectedMethod;
      const matchesSuitability = selectedSuitability === 'ALL' || item.mobileSuitability === selectedSuitability;
      const matchesCategory = selectedCategory === 'ALL' || item.categoryName === selectedCategory;

      return matchesSearch && matchesMethod && matchesSuitability && matchesCategory;
    });
  }, [apis, searchTerm, selectedMethod, selectedSuitability, selectedCategory]);

  const getMethodBadge = (method: string) => {
    switch (method) {
      case 'GET':
        return 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30';
      case 'POST':
        return 'bg-blue-500/15 text-blue-400 border-blue-500/30';
      case 'PUT':
        return 'bg-amber-500/15 text-amber-400 border-amber-500/30';
      case 'PATCH':
        return 'bg-purple-500/15 text-purple-400 border-purple-500/30';
      case 'DELETE':
        return 'bg-rose-500/15 text-rose-400 border-rose-500/30';
      default:
        return 'bg-slate-500/15 text-slate-400 border-slate-500/30';
    }
  };

  const getSuitabilityBadge = (suitability: string) => {
    switch (suitability) {
      case 'Direct':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
            <CheckCircle className="w-3 h-3" /> Reusable Direct
          </span>
        );
      case 'Optimizable':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs bg-amber-500/10 text-amber-400 border border-amber-500/30">
            <AlertTriangle className="w-3 h-3" /> Optimizable
          </span>
        );
      case 'Restricted':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs bg-rose-500/10 text-rose-400 border border-rose-500/30">
            <XCircle className="w-3 h-3" /> Excluded from Mobile
          </span>
        );
      default:
        return null;
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Search & Filter Bar */}
      <div className="bg-slate-900/90 border border-slate-800 rounded-xl p-4 shadow-sm">
        <div className="flex flex-col md:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              id="api-search-input"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search by endpoint, keyword, or Angular usage (e.g. gate-in, barcode, pm)..."
              className="w-full bg-slate-950/80 border border-slate-700/80 rounded-lg pl-10 pr-4 py-2 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <select
              id="filter-method-select"
              value={selectedMethod}
              onChange={(e) => setSelectedMethod(e.target.value)}
              aria-label="Filter by HTTP method"
              className="bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-blue-500"
            >
              <option value="ALL">All Methods</option>
              <option value="GET">GET</option>
              <option value="POST">POST</option>
              <option value="PUT">PUT</option>
              <option value="PATCH">PATCH</option>
              <option value="DELETE">DELETE</option>
            </select>

            <select
              id="filter-suitability-select"
              value={selectedSuitability}
              onChange={(e) => setSelectedSuitability(e.target.value)}
              aria-label="Filter by mobile suitability"
              className="bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-blue-500"
            >
              <option value="ALL">All Suitability</option>
              <option value="Direct">Reusable Direct</option>
              <option value="Optimizable">Optimizable DTO</option>
              <option value="Restricted">Excluded</option>
            </select>

            <select
              id="filter-category-select"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              aria-label="Filter by category"
              className="bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-blue-500 max-w-[180px] truncate"
            >
              {categories.map((c) => (
                <option key={c} value={c}>
                  {c === 'ALL' ? 'All Categories (32)' : c}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Counter Pill Bar */}
        <div className="flex items-center justify-between mt-3 pt-3 border-t border-slate-800 text-xs text-slate-400">
          <div>
            Showing <span className="text-white font-semibold">{filteredApis.length}</span> of{' '}
            <span className="text-white font-semibold">{apis.length}</span> documented endpoints
          </div>
          <div className="flex items-center gap-3">
            <span className="flex items-center gap-1.5 text-emerald-400">
              <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
              Direct: {apis.filter((a) => a.mobileSuitability === 'Direct').length}
            </span>
            <span className="flex items-center gap-1.5 text-amber-400">
              <span className="w-2 h-2 rounded-full bg-amber-400"></span>
              Optimizable: {apis.filter((a) => a.mobileSuitability === 'Optimizable').length}
            </span>
            <span className="flex items-center gap-1.5 text-rose-400">
              <span className="w-2 h-2 rounded-full bg-rose-400"></span>
              Excluded: {apis.filter((a) => a.mobileSuitability === 'Restricted').length}
            </span>
          </div>
        </div>
      </div>

      {/* API Table View */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-950/80 text-xs uppercase font-semibold text-slate-400 border-b border-slate-800">
              <tr>
                <th className="py-3 px-4">Method & Endpoint</th>
                <th className="py-3 px-4">Category</th>
                <th className="py-3 px-4">Security / Org</th>
                <th className="py-3 px-4">Suitability</th>
                <th className="py-3 px-4">Angular Web Usage</th>
                <th className="py-3 px-4 text-right">Inspect</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono text-xs">
              {filteredApis.map((api) => (
                <tr
                  key={api.id}
                  className="hover:bg-slate-800/40 transition-colors cursor-pointer group"
                  onClick={() => setActiveModalItem(api)}
                >
                  <td className="py-3.5 px-4 font-sans">
                    <div className="flex items-center gap-2 font-mono">
                      <span
                        className={`px-2 py-0.5 rounded text-[11px] font-bold border ${getMethodBadge(
                          api.method
                        )}`}
                      >
                        {api.method}
                      </span>
                      <span className="text-slate-200 font-medium group-hover:text-blue-400 transition-colors">
                        {api.endpoint}
                      </span>
                    </div>
                    <p className="text-xs text-slate-400 font-sans mt-0.5 line-clamp-1">{api.description}</p>
                  </td>

                  <td className="py-3.5 px-4 font-sans text-slate-300">
                    <span className="inline-block bg-slate-800/60 px-2 py-1 rounded text-xs">
                      {api.categoryName}
                    </span>
                  </td>

                  <td className="py-3.5 px-4 font-sans">
                    <div className="flex flex-col gap-1 text-[11px]">
                      <span className="flex items-center gap-1 text-slate-400">
                        <Shield className="w-3 h-3 text-blue-400" />
                        {api.auth} • {api.roles.join(', ')}
                      </span>
                      <span className="flex items-center gap-1 text-slate-500">
                        <Building2 className="w-3 h-3 text-purple-400" />
                        Org: {api.orgRequirement}
                      </span>
                    </div>
                  </td>

                  <td className="py-3.5 px-4 font-sans">{getSuitabilityBadge(api.mobileSuitability)}</td>

                  <td className="py-3.5 px-4 font-sans text-slate-400 text-xs">
                    <span className="line-clamp-1">{api.existingAngularUsage}</span>
                  </td>

                  <td className="py-3.5 px-4 text-right font-sans">
                    <button
                      id={`inspect-btn-${api.id}`}
                      className="text-xs text-blue-400 hover:text-blue-300 inline-flex items-center gap-1 px-2.5 py-1 rounded bg-blue-500/10 hover:bg-blue-500/20 border border-blue-500/30 transition-colors"
                    >
                      Inspect <ArrowUpRight className="w-3 h-3" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Inspection Modal */}
      {activeModalItem && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-6 shadow-2xl">
            <div className="flex items-start justify-between pb-4 border-b border-slate-800">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <span
                    className={`px-2.5 py-0.5 rounded text-xs font-bold border ${getMethodBadge(
                      activeModalItem.method
                    )}`}
                  >
                    {activeModalItem.method}
                  </span>
                  <h3 className="text-base font-bold text-white font-mono">{activeModalItem.endpoint}</h3>
                </div>
                <p className="text-xs text-slate-400 font-sans">{activeModalItem.description}</p>
              </div>
              <button
                id="close-modal-btn"
                onClick={() => setActiveModalItem(null)}
                className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="py-4 space-y-4 text-xs">
              {/* Meta Grid */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 bg-slate-950 p-3 rounded-xl border border-slate-800">
                <div>
                  <span className="text-slate-500 block">Category</span>
                  <span className="text-slate-200 font-medium">{activeModalItem.categoryName}</span>
                </div>
                <div>
                  <span className="text-slate-500 block">Authentication</span>
                  <span className="text-slate-200 font-medium">{activeModalItem.auth}</span>
                </div>
                <div>
                  <span className="text-slate-500 block">Org Requirement</span>
                  <span className="text-slate-200 font-medium">{activeModalItem.orgRequirement}</span>
                </div>
                <div>
                  <span className="text-slate-500 block">Pagination</span>
                  <span className="text-slate-200 font-medium">{activeModalItem.pagination}</span>
                </div>
              </div>

              <div>
                <span className="text-slate-400 block font-semibold mb-1">Allowed Roles (RBAC)</span>
                <div className="flex flex-wrap gap-1.5">
                  {activeModalItem.roles.map((r) => (
                    <span key={r} className="bg-slate-800 text-blue-300 px-2 py-0.5 rounded text-xs font-mono">
                      {r}
                    </span>
                  ))}
                </div>
              </div>

              <div>
                <span className="text-slate-400 block font-semibold mb-1">Request Model / DTO</span>
                <pre className="bg-slate-950 border border-slate-800 p-3 rounded-lg text-emerald-400 font-mono text-[11px] overflow-x-auto whitespace-pre-wrap">
                  {activeModalItem.requestModel}
                </pre>
              </div>

              <div>
                <span className="text-slate-400 block font-semibold mb-1">Response Model / DTO</span>
                <pre className="bg-slate-950 border border-slate-800 p-3 rounded-lg text-blue-400 font-mono text-[11px] overflow-x-auto whitespace-pre-wrap">
                  {activeModalItem.responseModel}
                </pre>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
                <div className="bg-slate-950/60 border border-slate-800 p-3 rounded-xl">
                  <span className="text-slate-400 font-semibold block mb-1">Existing Angular Web Usage</span>
                  <p className="text-slate-300">{activeModalItem.existingAngularUsage}</p>
                </div>
                <div className="bg-slate-950/60 border border-slate-800 p-3 rounded-xl">
                  <span className="text-slate-400 font-semibold block mb-1">Android Action Strategy</span>
                  <div className="mt-1">{getSuitabilityBadge(activeModalItem.mobileSuitability)}</div>
                  <p className="text-slate-400 mt-1 text-[11px]">Action: {activeModalItem.action}</p>
                </div>
              </div>
            </div>

            <div className="pt-4 border-t border-slate-800 flex justify-end">
              <button
                id="modal-done-btn"
                onClick={() => setActiveModalItem(null)}
                className="bg-blue-600 hover:bg-blue-500 text-white px-4 py-1.5 rounded-lg text-xs font-medium"
              >
                Close Inspector
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
