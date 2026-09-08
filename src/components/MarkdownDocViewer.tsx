import React, { useState } from 'react';
import { FileText, Copy, Check, Download, ExternalLink } from 'lucide-react';

interface DocFileItem {
  id: string;
  name: string;
  path: string;
  description: string;
  content: string;
}

interface MarkdownDocViewerProps {
  docs: DocFileItem[];
}

export const MarkdownDocViewer: React.FC<MarkdownDocViewerProps> = ({ docs }) => {
  const [selectedDocId, setSelectedDocId] = useState<string>(docs[0]?.id || '');
  const [copied, setCopied] = useState(false);

  const activeDoc = docs.find((d) => d.id === selectedDocId) || docs[0];

  const handleCopy = () => {
    if (!activeDoc) return;
    navigator.clipboard.writeText(activeDoc.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownload = () => {
    if (!activeDoc) return;
    const blob = new Blob([activeDoc.content], { type: 'text/markdown;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', activeDoc.name);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-6">
      {/* Overview Banner */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-base font-bold text-white mb-1 flex items-center gap-2">
            <FileText className="w-5 h-5 text-blue-400" />
            Generated Enterprise Documentation Artifacts
          </h2>
          <p className="text-xs text-slate-400">
            5 detailed architectural specification files generated in the project root{' '}
            <code className="text-blue-400 font-mono">/docs</code> folder.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            id="copy-doc-btn"
            onClick={handleCopy}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg text-xs font-medium border border-slate-700 transition-colors"
          >
            {copied ? (
              <>
                <Check className="w-3.5 h-3.5 text-emerald-400" />
                <span className="text-emerald-400">Copied!</span>
              </>
            ) : (
              <>
                <Copy className="w-3.5 h-3.5" />
                <span>Copy Content</span>
              </>
            )}
          </button>

          <button
            id="download-doc-btn"
            onClick={handleDownload}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 hover:bg-blue-500 text-white rounded-lg text-xs font-medium transition-colors"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Download .md</span>
          </button>
        </div>
      </div>

      {/* Document Selector Chips */}
      <div className="flex flex-wrap gap-2">
        {docs.map((doc) => {
          const isSelected = doc.id === activeDoc?.id;
          return (
            <button
              key={doc.id}
              id={`select-doc-${doc.id}`}
              onClick={() => setSelectedDocId(doc.id)}
              className={`flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-mono transition-all ${
                isSelected
                  ? 'bg-blue-600/20 text-blue-300 border border-blue-500/50 shadow-sm'
                  : 'bg-slate-900 text-slate-400 border border-slate-800 hover:text-slate-200 hover:bg-slate-850'
              }`}
            >
              <FileText className="w-3.5 h-3.5" />
              <span>{doc.name}</span>
            </button>
          );
        })}
      </div>

      {/* Markdown Document Content Box */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
        <div className="bg-slate-950 px-4 py-3 border-b border-slate-800 flex items-center justify-between text-xs">
          <div className="flex items-center gap-2">
            <span className="font-mono text-blue-400 font-semibold">{activeDoc?.path}</span>
            <span className="text-slate-500">•</span>
            <span className="text-slate-400">{activeDoc?.description}</span>
          </div>
          <span className="text-[11px] font-mono text-slate-500">Markdown Format</span>
        </div>

        <div className="p-5 max-h-[650px] overflow-y-auto bg-slate-950/60 font-mono text-xs text-slate-300 leading-relaxed whitespace-pre-wrap selection:bg-blue-500/30">
          {activeDoc?.content}
        </div>
      </div>
    </div>
  );
};
