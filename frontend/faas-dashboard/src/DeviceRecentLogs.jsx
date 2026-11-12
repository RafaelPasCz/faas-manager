import { useState, useEffect } from 'react';
import { 
  Table, 
  TableHead, 
  TableRow, 
  TableHeaderCell, 
  TableBody, 
  TableCell, 
  Badge, 
  Text 
} from '@tremor/react';

export default function DeviceRecentLogs({ deviceId }) {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchLogs = async () => {
      try {
        const response = await fetch(`http://localhost:8080/api/execution-logs/find-all`);
        if (response.ok) {
          const data = await response.json();
          
          const deviceLogs = data.filter(log => 
            log.functionInstance?.device?.id === deviceId
          );

          const sortedLogs = deviceLogs.sort((a, b) => 
            new Date(b.executedAt) - new Date(a.executedAt)
          );

          setLogs(sortedLogs.slice(0, 5));
        }
      } catch (error) {
        console.error("Erro ao buscar logs do dispositivo:", error);
      } finally {
        setLoading(false);
      }
    };

    if (deviceId) {
      fetchLogs();
      const interval = setInterval(fetchLogs, 10000);
      return () => clearInterval(interval);
    }
  }, [deviceId]);

  const formatTime = (isoString) => {
    if (!isoString) return '';
    const date = new Date(isoString);
    return date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  };

  if (loading) {
    return (
      <div className="mt-8 border-t border-gray-200 dark:border-zinc-700 pt-5 text-center">
        <Text className="text-zinc-500 text-sm">Carregando execuções...</Text>
      </div>
    );
  }

  return (
    <div className="mt-8 border-t border-gray-200 dark:border-zinc-700 pt-5">
      <Text className="font-semibold text-slate-800 dark:text-zinc-300 mb-4 text-sm">
        Últimas Execuções no Dispositivo
      </Text>
      
      {logs.length === 0 ? (
        <div className="bg-gray-50 dark:bg-zinc-800/50 p-4 rounded-lg border border-gray-100 dark:border-zinc-700/60 text-center">
           <Text className="text-sm text-slate-500 dark:text-zinc-500 italic">
             Nenhuma execução recente registrada neste dispositivo.
           </Text>
        </div>
      ) : (
        <div className="border border-gray-200 dark:border-zinc-700 rounded-lg overflow-hidden bg-white dark:bg-zinc-900 shadow-sm">
          <Table>
            <TableHead className="bg-gray-50 dark:bg-zinc-800">
              <TableRow className="border-b border-gray-200 dark:border-zinc-700">
                <TableHeaderCell className="text-gray-600 dark:text-zinc-300 font-semibold text-xs">Função</TableHeaderCell>
                <TableHeaderCell className="text-gray-600 dark:text-zinc-300 font-semibold text-xs">Horário</TableHeaderCell>
                <TableHeaderCell className="text-gray-600 dark:text-zinc-300 font-semibold text-xs text-right">Duração</TableHeaderCell>
                <TableHeaderCell className="text-gray-600 dark:text-zinc-300 font-semibold text-xs text-right">Status</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {logs.map((log) => (
                <TableRow 
                  key={log.id} 
                  className="hover:bg-gray-50 dark:hover:bg-zinc-800/50 transition-colors border-b border-gray-100 dark:border-zinc-800"
                >
                  <TableCell>
                    <Text className="font-medium text-slate-800 dark:text-zinc-200 text-xs">
                      {log.functionInstance?.function?.name || 'N/A'}
                    </Text>
                  </TableCell>
                  <TableCell>
                    <Text className="text-slate-600 dark:text-zinc-400 text-xs">
                      {formatTime(log.executedAt)}
                    </Text>
                  </TableCell>
                  <TableCell className="text-right">
                    <Text className="text-slate-600 dark:text-zinc-400 font-mono text-xs">
                      {log.requestDurationMs ? `${log.requestDurationMs}ms` : '-'}
                    </Text>
                  </TableCell>
                  <TableCell className="text-right">
                    <Badge 
                      color={log.success ? 'emerald' : 'red'} 
                      className="dark:bg-transparent dark:border dark:border-zinc-600 px-2 py-0.5"
                    >
                      {log.statusCode || 'N/A'}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}