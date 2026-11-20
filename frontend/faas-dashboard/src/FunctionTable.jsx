import { useCallback, useEffect, useState } from 'react';
import {
  Table,
  TableHead,
  TableRow,
  TableHeaderCell,
  TableBody,
  TableCell,
  Badge,
  Text,
} from '@tremor/react';

export default function FunctionTable({ deviceId, refreshTrigger }) {
  const [instances, setInstances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState(null);
  const [redeployingId, setRedeployingId] = useState(null);

  const fetchInstances = useCallback(async () => {
    if (!deviceId) return;
    try {
      const response = await fetch(`http://localhost:8080/api/function-instances/device/${deviceId}`);
      if (response.ok) {
        const data = await response.json();
        setInstances(data.filter(instance => instance.active !== false));
      }
    } catch (error) {
      console.error("Erro ao buscar instâncias da função:", error);
    } finally {
      setLoading(false);
    }
  }, [deviceId]);

  useEffect(() => {
    if (deviceId) {
      fetchInstances();
      const interval = setInterval(fetchInstances, 15000);
      return () => clearInterval(interval);
    }
  }, [deviceId, refreshTrigger, fetchInstances]);

  const handleUndeploy = async (e, instance) => {
    e.stopPropagation();
    const funcName = instance.function?.name || `#${instance.id}`;
    if (!window.confirm(`Remover a função "${funcName}" deste dispositivo?`)) return;

    setDeletingId(instance.id);
    try {
      const response = await fetch(`http://localhost:8080/api/function-instances/${instance.id}`, {
        method: 'DELETE',
      });
      if (response.ok) {
        await fetchInstances();
      } else {
        const errData = await response.json().catch(() => null);
        alert(errData?.message || 'Falha ao remover a função do dispositivo.');
      }
    } catch (error) {
      console.error("Erro de rede ao remover instância:", error);
      alert('Erro de rede ao tentar remover a instância.');
    } finally {
      setDeletingId(null);
    }
  };

  const handleRedeploy = async (e, instance) => {
    e.stopPropagation();
    setRedeployingId(instance.id);
    try {
      const response = await fetch(`http://localhost:8080/api/function-instances/${instance.id}/redeploy`, {
        method: 'POST',
      });
      if (response.ok) {
        await fetchInstances();
      } else {
        const errData = await response.json().catch(() => null);
        alert(errData?.message || 'Falha ao re-implantar a função no agente.');
      }
    } catch (error) {
      console.error("Erro de rede ao re-implantar instância:", error);
      alert('Erro de rede ao tentar re-implantar a instância.');
    } finally {
      setRedeployingId(null);
    }
  };

  const getStatusColor = (status) => {
    if (!status) return 'gray';
    switch (status.toUpperCase()) {
      case 'IDLE': return 'slate';
      case 'EXECUTING':
      case 'RUNNING': return 'emerald';
      case 'DEPLOYING': return 'amber';
      case 'STOPPED': return 'gray';
      case 'MISSING':
      case 'ERROR': return 'red';
      default: return 'gray';
    }
  };

  if (loading) {
    return <div className="mt-4 p-4 text-center text-zinc-500">Carregando instâncias...</div>;
  }

  return (
    <div className="mt-4 border border-gray-200 dark:border-zinc-700 rounded-lg overflow-hidden bg-white dark:bg-zinc-900 shadow-sm">
      <Table>
        <TableHead className="bg-gray-50 dark:bg-zinc-800">
          <TableRow className="border-b border-gray-200 dark:border-zinc-700">
            <TableHeaderCell className="text-gray-600 dark:text-zinc-300 font-semibold">Função</TableHeaderCell>
            <TableHeaderCell className="text-gray-600 dark:text-zinc-300 font-semibold">Runtime</TableHeaderCell>
            <TableHeaderCell className="text-gray-600 dark:text-zinc-300 font-semibold text-right">Status</TableHeaderCell>
            <TableHeaderCell className="text-gray-600 dark:text-zinc-300 font-semibold text-right">Ações</TableHeaderCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {instances.length === 0 ? (
            <TableRow>
              <TableCell colSpan={4} className="text-center py-6 text-gray-500 dark:text-zinc-500">
                Nenhuma função instanciada neste dispositivo.
              </TableCell>
            </TableRow>
          ) : (
            instances.map((instance) => (
              <TableRow 
                key={instance.id} 
                className="hover:bg-gray-50 dark:hover:bg-zinc-800/50 transition-colors border-b border-gray-100 dark:border-zinc-800"
              >
                <TableCell>
                  <Text className="font-medium text-slate-800 dark:text-zinc-200">
                    {instance.function?.name || 'N/A'}
                  </Text>
                  {instance.status === 'MISSING' && (
                    <Text className="text-xs text-red-600 dark:text-red-400 mt-0.5">
                      Não encontrada no agente. Deseja re-implantar?
                    </Text>
                  )}
                </TableCell>
                <TableCell>
                  <Text className="text-slate-600 dark:text-zinc-400">
                    {instance.function?.runtime || 'N/A'}
                  </Text>
                </TableCell>
                <TableCell className="text-right">
                  <Badge color={getStatusColor(instance.status)} className="dark:bg-transparent dark:border dark:border-zinc-600">
                    {instance.status || 'UNKNOWN'}
                  </Badge>
                </TableCell>
                <TableCell className="text-right">
                  <div className="inline-flex items-center justify-end gap-2">
                    {instance.status === 'MISSING' && (
                      <button
                        onClick={(e) => handleRedeploy(e, instance)}
                        disabled={redeployingId === instance.id || deletingId === instance.id}
                        className="px-2 py-1 text-xs font-medium rounded-md bg-emerald-600 text-white hover:bg-emerald-700 dark:bg-emerald-500 dark:text-zinc-900 dark:hover:bg-emerald-400 transition-colors disabled:opacity-50"
                        title="Re-implantar função no agente"
                      >
                        {redeployingId === instance.id ? 'Re-implantando...' : 'Re-implantar'}
                      </button>
                    )}
                    <button
                      onClick={(e) => handleUndeploy(e, instance)}
                      disabled={deletingId === instance.id || redeployingId === instance.id}
                      className="p-1.5 rounded-md text-gray-400 hover:text-red-600 hover:bg-red-50 dark:text-zinc-500 dark:hover:text-red-400 dark:hover:bg-red-900/30 transition-colors disabled:opacity-50"
                      title="Remover implantação"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  );
}
