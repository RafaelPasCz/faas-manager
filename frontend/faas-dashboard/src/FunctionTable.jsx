import { useEffect, useState } from 'react';
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

  useEffect(() => {
    const fetchInstances = async () => {
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
    };

    if (deviceId) {
      fetchInstances();
      const interval = setInterval(fetchInstances, 15000);
      return () => clearInterval(interval);
    }
  }, [deviceId, refreshTrigger]);

  const getStatusColor = (status) => {
    if (!status) return 'gray';
    switch (status.toUpperCase()) {
      case 'IDLE': return 'slate';
      case 'EXECUTING': return 'emerald';
      case 'DEPLOYING': return 'amber';
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
          </TableRow>
        </TableHead>
        <TableBody>
          {instances.length === 0 ? (
            <TableRow>
              <TableCell colSpan={3} className="text-center py-6 text-gray-500 dark:text-zinc-500">
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
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  );
}