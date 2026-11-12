import { useState, useEffect } from 'react';
import { 
  Card, 
  Text, 
  Metric, 
  Badge, 
  Table, 
  TableHead, 
  TableRow, 
  TableHeaderCell, 
  TableBody, 
  TableCell 
} from '@tremor/react';

export default function FunctionCard({ func, onFunctionDeleted }) {
  const [isExpanded, setIsExpanded] = useState(false);
  const [instances, setInstances] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

 
  useEffect(() => {
    if (isExpanded) {
      fetchInstances();
    }
  }, [isExpanded]);

  const fetchInstances = async () => {
    setLoading(true);
    try {
     
      const response = await fetch(`http://localhost:8080/api/function-instances/function/${func.id}`);
      
      if (response.ok) {
        const data = await response.json();
        setInstances(data.filter(inst => inst.active !== false));
      }
    } catch (error) {
      console.error("Erro ao buscar instâncias:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (e) => {
    e.stopPropagation(); 
    
    const confirmDelete = window.confirm(`Tem certeza que deseja excluir a função "${func.name}"?`);
    if (!confirmDelete) return;

    setIsDeleting(true);
    
    try {
      const response = await fetch(`http://localhost:8080/api/function/${func.id}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        if (onFunctionDeleted) onFunctionDeleted();
      } else {
        alert('Falha ao excluir a função.');
      }
    } catch (error) {
      console.error('Erro de conexão ao deletar:', error);
      alert('Erro de rede ao tentar excluir.');
    } finally {
      setIsDeleting(false);
    }
  };

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

  return (
    <div className={`transition-all duration-500 ease-in-out ${isExpanded ? 'col-span-1 sm:col-span-2 lg:col-span-3 order-first' : 'col-span-1'}`}>
      <Card 
        className={`h-full flex flex-col cursor-pointer bg-white dark:bg-zinc-800 border-transparent dark:border-zinc-700 hover:ring-2 hover:ring-emerald-500 transition-all shadow-sm ${isDeleting ? 'opacity-50 pointer-events-none' : ''}`}
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <div className="flex justify-between items-start">
          <div>
            <Text className="dark:text-zinc-400 font-mono text-xs mb-1">{func.dockerImage}</Text>
            <Metric className="mt-1 text-2xl dark:text-white">{func.name}</Metric>
          </div>
          
          {/* Botão de Excluir */}
          <button 
            onClick={handleDelete}
            disabled={isDeleting}
            className="p-2 rounded-full transition-colors bg-gray-50 text-gray-400 hover:bg-red-50 hover:text-red-600 dark:bg-zinc-900 dark:text-zinc-500 dark:hover:bg-red-900/30 dark:hover:text-red-400"
            title="Excluir Função"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
          </button>
        </div>

        <div className="mt-4">
          <Badge color="slate" className="dark:bg-zinc-700 dark:text-zinc-300">
            Runtime: {func.runtime}
          </Badge>
        </div>

        {/* Área Expandida */}
        {isExpanded && (
          <div className="mt-8 pt-6 border-t border-gray-200 dark:border-zinc-700 cursor-default" onClick={(e) => e.stopPropagation()}>
            <Metric className="text-lg mb-4 dark:text-white">Implantações Ativas</Metric>
            
            {loading ? (
              <Text className="text-center py-4 dark:text-zinc-500">Carregando instâncias...</Text>
            ) : (
              <div className="border border-gray-200 dark:border-zinc-700 rounded-lg overflow-hidden bg-white dark:bg-zinc-900 shadow-sm">
                <Table>
                  <TableHead className="bg-gray-50 dark:bg-zinc-800">
                    <TableRow className="border-b border-gray-200 dark:border-zinc-700">
                      <TableHeaderCell className="text-gray-600 dark:text-zinc-300 font-semibold">Dispositivo Hospedeiro</TableHeaderCell>
                      <TableHeaderCell className="text-gray-600 dark:text-zinc-300 font-semibold">IP</TableHeaderCell>
                      <TableHeaderCell className="text-gray-600 dark:text-zinc-300 font-semibold text-right">Status</TableHeaderCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {instances.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={3} className="text-center py-6 text-gray-500 dark:text-zinc-500">
                          Esta função não está implantada em nenhum dispositivo no momento.
                        </TableCell>
                      </TableRow>
                    ) : (
                      instances.map((instance) => (
                        <TableRow key={instance.id} className="hover:bg-gray-50 dark:hover:bg-zinc-800/50 transition-colors border-b border-gray-100 dark:border-zinc-800">
                          <TableCell>
                            <Text className="font-medium text-slate-800 dark:text-zinc-200">
                              {instance.device?.name || 'N/A'}
                            </Text>
                          </TableCell>
                          <TableCell>
                            <Text className="text-slate-600 dark:text-zinc-400 font-mono text-xs">
                              {instance.device?.ipAddress || 'N/A'}
                            </Text>
                          </TableCell>
                          <TableCell className="text-right">
                            <Badge color={getStatusColor(instance.status)} className="dark:bg-transparent dark:border dark:border-zinc-600">
                              {instance.status}
                            </Badge>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </div>
            )}
          </div>
        )}
      </Card>
    </div>
  );
}