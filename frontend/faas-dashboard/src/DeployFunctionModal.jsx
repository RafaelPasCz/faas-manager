import { useState, useEffect } from 'react';
import { Button, Text } from '@tremor/react';

export default function DeployFunctionModal({ isOpen, onClose, deviceId, onDeploySuccess }) {
  const [functions, setFunctions] = useState([]);
  const [selectedFunctionId, setSelectedFunctionId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);


  useEffect(() => {
    if (isOpen) {
      fetchFunctions();
      setSelectedFunctionId('');
      setError(null);
    }
  }, [isOpen]);

  const fetchFunctions = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/function/find-all');
      if (response.ok) {
        const data = await response.json();
        setFunctions(data.filter(f => f.active !== false));
      }
    } catch (err) {
      console.error("Erro ao buscar catálogo de funções:", err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedFunctionId) {
      setError("Selecione uma função para fazer o deploy.");
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const url = `http://localhost:8080/api/function-instances/deploy?functionId=${selectedFunctionId}&deviceId=${deviceId}`;
      
      const response = await fetch(url, { method: 'POST' });

      if (response.ok) {
        onDeploySuccess();
        onClose();
      } else {
        const errData = await response.json().catch(() => null);
        setError(errData?.message || 'Erro ao realizar deploy da função.');
      }
    } catch (err) {
      setError('Erro de rede ao tentar conectar com o servidor.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm transition-opacity" onClick={onClose}>
      <div className="bg-white dark:bg-zinc-900 rounded-xl shadow-2xl w-full max-w-md p-6 border border-gray-200 dark:border-zinc-800" onClick={e => e.stopPropagation()}>
        <h2 className="text-xl font-semibold text-slate-800 dark:text-white mb-1">
          Fazer Deploy de Função
        </h2>
        <Text className="dark:text-zinc-400 mb-6">
          Selecione uma função do catálogo para provisionar neste dispositivo.
        </Text>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-zinc-300 mb-2">
              Função Abstrata *
            </label>
            {/* Select Nativo do HTML estilizado com Tailwind */}
            <select 
              value={selectedFunctionId} 
              onChange={(e) => setSelectedFunctionId(e.target.value)}
              className="w-full rounded-md border border-gray-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 px-3 py-2 text-sm text-gray-900 dark:text-zinc-200 shadow-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500 transition-colors"
              required
            >
              <option value="" disabled>Selecione uma função...</option>
              {functions.map((func) => (
                <option key={func.id} value={func.id}>
                  {func.name} ({func.runtime})
                </option>
              ))}
            </select>
          </div>

          {error && (
            <div className="p-3 bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400 text-sm rounded-lg border border-red-200 dark:border-red-800">
              {error}
            </div>
          )}

          <div className="flex justify-end gap-3 mt-8">
            <Button 
              type="button" 
              variant="light" 
              color="slate" 
              onClick={onClose}
              disabled={isSubmitting}
            >
              Cancelar
            </Button>
            <Button 
              type="submit" 
              color="emerald" 
              disabled={isSubmitting}
              className="bg-emerald-600 hover:bg-emerald-700 dark:bg-emerald-500 dark:hover:bg-emerald-400 dark:text-zinc-900 border-transparent"
            >
              {isSubmitting ? 'Provisionando...' : 'Confirmar Deploy'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}