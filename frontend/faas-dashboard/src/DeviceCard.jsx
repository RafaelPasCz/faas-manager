import { useState } from 'react';
import FunctionTable from './FunctionTable';
import { Card, Text, Metric, Badge, Button, Grid } from '@tremor/react';
import DeviceMetrics from './DeviceMetrics';
import DeployFunctionModal from './DeployFunctionModal';
import DeviceRecentLogs from './DeviceRecentLogs';

export default function DeviceCard({ device, allDevices, onDeviceDeleted }) {
  const pinKey = `faas_pinned_${device.id}`;
  const [isPinned, setIsPinned] = useState(() => localStorage.getItem(pinKey) === 'true');
  const [isExpanded, setIsExpanded] = useState(isPinned);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isDeployModalOpen, setIsDeployModalOpen] = useState(false); 
  const [refreshTrigger, setRefreshTrigger] = useState(0); 
  

  const togglePin = (e) => {
    e.stopPropagation(); 
    const newState = !isPinned;
    setIsPinned(newState);
    localStorage.setItem(pinKey, newState.toString());
  };

  const handleDelete = async (e) => {
    e.stopPropagation(); 
    
    const confirmDelete = window.confirm(`Tem certeza que deseja excluir o dispositivo "${device.name}"?`);
    if (!confirmDelete) return;

    setIsDeleting(true);
    
    try {
      const response = await fetch(`http://localhost:8080/api/devices/${device.id}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        if (onDeviceDeleted) {
          onDeviceDeleted();
        }
      } else {
        console.error('Erro ao deletar dispositivo:', await response.text());
        alert('Falha ao excluir o dispositivo.');
      }
    } catch (error) {
      console.error('Erro de conexão ao deletar:', error);
      alert('Erro de rede ao tentar excluir.');
    } finally {
      setIsDeleting(false);
    }
  };

  const associatedDevices = allDevices.filter(d => d.prometheusHostId === device.id);

  return (
    <div className={`transition-all duration-500 ease-in-out ${isExpanded ? 'col-span-1 sm:col-span-2 lg:col-span-3 order-first' : 'col-span-1'}`}>
      <Card 
        decoration="top" 
        decorationColor={device.isFunctionHost ? "emerald" : "slate"}
        className={`h-full flex flex-col cursor-pointer bg-white dark:bg-zinc-800 border-transparent dark:border-zinc-700 hover:ring-2 hover:ring-emerald-500 dark:hover:ring-emerald-500 transition-all shadow-sm ${isDeleting ? 'opacity-50 pointer-events-none' : ''}`}
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <div className="flex justify-between items-start">
          <div>
            <Text className="dark:text-zinc-400">{device.ipAddress}</Text>
            <Metric className="mt-1 text-2xl dark:text-white">{device.name}</Metric>
          </div>
          
          <div className="flex gap-2">
            <button 
              onClick={handleDelete}
              disabled={isDeleting}
              className="p-2 rounded-full transition-colors bg-gray-50 text-gray-400 hover:bg-red-50 hover:text-red-600 dark:bg-zinc-900 dark:text-zinc-500 dark:hover:bg-red-900/30 dark:hover:text-red-400"
              title="Excluir Dispositivo"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>

            <button 
              onClick={togglePin}
              className={`p-2 rounded-full transition-colors ${isPinned ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-gray-50 text-gray-400 hover:text-emerald-600 dark:bg-zinc-900 dark:text-zinc-500 dark:hover:text-emerald-400'}`}
              title="Fixar Dispositivo"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path d="M5 4a2 2 0 012-2h6a2 2 0 012 2v14l-5-2.5L5 18V4z" />
              </svg>
            </button>
          </div>
        </div>

        <div className="mt-4 flex gap-2">
          {device.isPrometheusHost && <Badge color="slate">Prometheus Host</Badge>}
          {device.isFunctionHost && <Badge color="emerald">Function Host</Badge>}
        </div>

        <div onClick={(e) => e.stopPropagation()}>
           <DeviceMetrics deviceId={device.id} />
        </div>

        {isExpanded && (
          <div className="mt-8 pt-6 border-t border-gray-200 dark:border-zinc-700 cursor-default" onClick={(e) => e.stopPropagation()}>
            
            {device.isPrometheusHost && (
              <div className="mb-6">
                <Metric className="text-lg mb-3 dark:text-white">Dispositivos Associados</Metric>
                {associatedDevices.length > 0 ? (
                  <Grid numItemsSm={1} numItemsLg={3} className="gap-4">
                    {associatedDevices.map(assoc => (
                      <Card key={assoc.id} className="bg-slate-50 dark:bg-zinc-900 p-4 border border-gray-200 dark:border-zinc-700 shadow-sm hover:border-emerald-500 dark:hover:border-emerald-500 transition-colors">
                        <Text className="font-semibold dark:text-white">{assoc.name}</Text>
                        <Text className="text-xs dark:text-zinc-400">{assoc.ipAddress}</Text>
                        <Button size="xs" variant="light" color="emerald" className="mt-2 text-xs">Ver Detalhes</Button>
                      </Card>
                    ))}
                  </Grid>
                ) : (
                  <Text className="text-gray-400 dark:text-zinc-500 italic">Nenhum nó associado a este servidor.</Text>
                )}
              </div>
            )}

            {device.isFunctionHost && (
              <div>
                <div className="flex items-center justify-between mb-3">
                  <Metric className="text-lg dark:text-white">Instâncias em Execução</Metric>
                  <Button 
                    size="xs" 
                    variant="light" 
                    color="emerald"
                    onClick={(e) => {
                      e.stopPropagation(); 
                      setIsDeployModalOpen(true);
                    }}
                  >
                    + Fazer Deploy
                  </Button>
                </div>
                
                <FunctionTable deviceId={device.id} refreshTrigger={refreshTrigger} />
                
                <DeployFunctionModal 
                  isOpen={isDeployModalOpen}
                  onClose={() => setIsDeployModalOpen(false)}
                  deviceId={device.id}
                  onDeploySuccess={() => {
                    setRefreshTrigger(prev => prev + 1);
                  }}
                />

                <DeviceRecentLogs deviceId={device.id} />

              </div>
            )}
          </div> 
        )}
      </Card>
    </div>
  );
}