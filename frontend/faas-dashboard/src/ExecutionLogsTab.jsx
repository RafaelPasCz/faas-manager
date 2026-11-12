import { useState, useEffect } from 'react';
import {
  Card,
  Table,
  TableHead,
  TableRow,
  TableHeaderCell,
  TableBody,
  TableCell,
  Badge,
  Text,
  TextInput
} from '@tremor/react';

export default function ExecutionLogsTab() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);

  const [searchTerm, setSearchTerm] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [resultFilter, setResultFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');

  useEffect(() => {
    fetchLogs();
  }, []);

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/execution-logs/find-all');
      if (response.ok) {
        const data = await response.json();
        const sortedData = data.sort((a, b) => new Date(b.executedAt) - new Date(a.executedAt));
        setLogs(sortedData);
      }
    } catch (error) {
      console.error("Erro ao buscar logs:", error);
    } finally {
      setLoading(false);
    }
  };

  const filteredLogs = logs.filter(log => {

    const term = searchTerm.toLowerCase();
    const funcName = log.functionInstance?.function?.name?.toLowerCase() || '';
    const devName = log.functionInstance?.device?.name?.toLowerCase() || '';
    const matchesSearch = funcName.includes(term) || devName.includes(term);

 
    const matchesResult = resultFilter === 'ALL' 
      ? true 
      : resultFilter === 'SUCCESS' ? log.success === true : log.success === false;


    const matchesStatus = statusFilter === 'ALL'
      ? true
      : String(log.statusCode) === statusFilter;

    let matchesDate = true;
    const logDate = new Date(log.executedAt);
    
    if (startDate) {
      const start = new Date(startDate);
      if (logDate < start) matchesDate = false;
    }
    
    if (endDate) {
      const end = new Date(endDate);
      if (logDate > end) matchesDate = false;
    }

    return matchesSearch && matchesResult && matchesStatus && matchesDate;
  });

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    const date = new Date(isoString);
    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    }).format(date);
  };

  const uniqueStatusCodes = [...new Set(logs.map(log => log.statusCode).filter(Boolean))].sort();

  return (
    <div className="mt-4">
      <div className="mb-6 bg-white dark:bg-zinc-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-zinc-700 flex flex-wrap gap-4 items-end transition-colors">
        
        <div className="flex-1 min-w-[200px]">
          <label className="block text-xs text-gray-500 dark:text-zinc-400 mb-1">Pesquisa Geral</label>
          <TextInput 
            placeholder="Função ou dispositivo..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="flex-1 min-w-[180px]">
          <label className="block text-xs text-gray-500 dark:text-zinc-400 mb-1">A partir de (Data/Hora)</label>
          <input 
            type="datetime-local" 
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className="w-full rounded-md border border-gray-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 px-3 py-2 text-sm text-gray-900 dark:text-zinc-200 shadow-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500 h-[38px] transition-colors"
          />
        </div>

        <div className="flex-1 min-w-[180px]">
          <label className="block text-xs text-gray-500 dark:text-zinc-400 mb-1">Até (Data/Hora)</label>
          <input 
            type="datetime-local" 
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="w-full rounded-md border border-gray-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 px-3 py-2 text-sm text-gray-900 dark:text-zinc-200 shadow-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500 h-[38px] transition-colors"
          />
        </div>

        <div className="flex-1 min-w-[130px]">
          <label className="block text-xs text-gray-500 dark:text-zinc-400 mb-1">Resultado</label>
          <select 
            value={resultFilter} 
            onChange={(e) => setResultFilter(e.target.value)}
            className="w-full rounded-md border border-gray-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 px-3 py-2 text-sm text-gray-900 dark:text-zinc-200 shadow-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500 h-[38px] transition-colors"
          >
            <option value="ALL">Todos</option>
            <option value="SUCCESS">Sucesso</option>
            <option value="FAILED">Falha</option>
          </select>
        </div>

        <div className="flex-1 min-w-[130px]">
          <label className="block text-xs text-gray-500 dark:text-zinc-400 mb-1">Status Code</label>
          <select 
            value={statusFilter} 
            onChange={(e) => setStatusFilter(e.target.value)}
            className="w-full rounded-md border border-gray-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 px-3 py-2 text-sm text-gray-900 dark:text-zinc-200 shadow-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500 h-[38px] transition-colors"
          >
            <option value="ALL">Todos</option>
            {uniqueStatusCodes.map(code => (
              <option key={code} value={String(code)}>{code}</option>
            ))}
          </select>
        </div>
      </div>

      <Card className="bg-white dark:bg-zinc-900 border-gray-200 dark:border-zinc-800 shadow-sm overflow-hidden p-0">
        {loading ? (
          <div className="p-8 text-center text-zinc-500">Carregando histórico de logs...</div>
        ) : (
          <Table>
            <TableHead className="bg-gray-50 dark:bg-zinc-800 border-b border-gray-200 dark:border-zinc-700">
              <TableRow>
                <TableHeaderCell className="text-gray-600 dark:text-zinc-300">Data/Hora</TableHeaderCell>
                <TableHeaderCell className="text-gray-600 dark:text-zinc-300">Função</TableHeaderCell>
                <TableHeaderCell className="text-gray-600 dark:text-zinc-300">Dispositivo</TableHeaderCell>
                <TableHeaderCell className="text-gray-600 dark:text-zinc-300 text-right">Duração</TableHeaderCell>
                <TableHeaderCell className="text-gray-600 dark:text-zinc-300 text-right">Status Code</TableHeaderCell>
                <TableHeaderCell className="text-gray-600 dark:text-zinc-300 text-right">Resultado</TableHeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredLogs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center py-8 text-gray-500 dark:text-zinc-500">
                    Nenhum log de execução encontrado para estes filtros.
                  </TableCell>
                </TableRow>
              ) : (
                filteredLogs.map((log) => (
                  <TableRow key={log.id} className="hover:bg-gray-50 dark:hover:bg-zinc-800/50 border-b border-gray-100 dark:border-zinc-800">
                    <TableCell>
                      <Text className="text-slate-600 dark:text-zinc-400 text-sm">
                        {formatDate(log.executedAt)}
                      </Text>
                    </TableCell>
                    <TableCell>
                      <Text className="font-medium text-slate-800 dark:text-zinc-200">
                        {log.functionInstance?.function?.name || 'N/A'}
                      </Text>
                    </TableCell>
                    <TableCell>
                      <Text className="text-slate-600 dark:text-zinc-400">
                        {log.functionInstance?.device?.name || 'N/A'}
                      </Text>
                    </TableCell>
                    <TableCell className="text-right">
                      <Text className="text-slate-600 dark:text-zinc-400 font-mono text-sm">
                        {log.requestDurationMs ? `${log.requestDurationMs}ms` : '-'}
                      </Text>
                    </TableCell>
                    <TableCell className="text-right">
                      <Text className={`font-mono text-sm ${log.statusCode >= 400 ? 'text-red-500' : 'text-emerald-500 dark:text-emerald-400'}`}>
                        {log.statusCode || '-'}
                      </Text>
                    </TableCell>
                    <TableCell className="text-right">
                      <Badge color={log.success ? 'emerald' : 'red'} className="dark:bg-transparent dark:border dark:border-zinc-600">
                        {log.success ? 'SUCCESS' : 'FAILED'}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        )}
      </Card>
    </div>
  );
}