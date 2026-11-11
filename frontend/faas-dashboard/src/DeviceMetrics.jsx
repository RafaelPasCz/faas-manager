import { useEffect, useState } from 'react';
import { Text } from '@tremor/react';

export default function DeviceMetrics({ deviceId }) {
  const [cpu, setCpu] = useState(0);
  const [ram, setRam] = useState(0);

  const fetchMetrics = async () => {
    try {
      const response = await fetch(`http://localhost:8080/api/devices/${deviceId}/metrics`);
      
      if (response.ok) {
        const data = await response.json();
        setCpu(data.cpu || 0);
        setRam(data.ram || 0);
      }
    } catch (error) {
      console.error(`Erro ao buscar métricas:`, error);
    }
  };

  useEffect(() => {
    fetchMetrics(); 
    const interval = setInterval(fetchMetrics, 10000); 
    return () => clearInterval(interval); 
  }, [deviceId]);

  // Garante que o CSS não quebre com valores bizarros
  const safeCpu = Math.min(Math.max(cpu, 0), 100);
  const safeRam = Math.min(Math.max(ram, 0), 100);

  return (
    <div className="mt-6 space-y-4">
      <div>
        <div className="flex justify-between items-center mb-1">
          <Text className="dark:text-zinc-300">CPU Uso</Text>
          <Text className="dark:text-zinc-300">{cpu.toFixed(1)}%</Text>
        </div>
        {/* Barra de progresso nativa Tailwind */}
        <div className="w-full bg-slate-200 dark:bg-zinc-700 rounded-full h-2 overflow-hidden">
          <div 
            className="bg-blue-500 h-2 rounded-full transition-all duration-500 ease-out" 
            style={{ width: `${safeCpu}%` }}
          ></div>
        </div>
      </div>

      <div>
        <div className="flex justify-between items-center mb-1">
          <Text className="dark:text-zinc-300">RAM Uso</Text>
          <Text className="dark:text-zinc-300">{ram.toFixed(1)}%</Text>
        </div>
        {/* Barra de progresso nativa Tailwind */}
        <div className="w-full bg-slate-200 dark:bg-zinc-700 rounded-full h-2 overflow-hidden">
          <div 
            className="bg-emerald-500 h-2 rounded-full transition-all duration-500 ease-out" 
            style={{ width: `${safeRam}%` }}
          ></div>
        </div>
      </div>
    </div>
  );
}