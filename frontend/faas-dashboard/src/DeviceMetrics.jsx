import { useEffect, useState } from 'react';
import { ProgressBar, Text } from '@tremor/react';

export default function DeviceMetrics({ deviceId }) {
  const [cpu, setCpu] = useState(0);
  const [ram, setRam] = useState(0);

  const fetchMetrics = async () => {
    try {
      // Busca métrica de RAM
      const ramRes = await fetch(`http://localhost:8080/api/devices/${deviceId}/metrics?type=ram`);
      const ramData = await ramRes.json();
      if (ramData.data?.result?.length > 0) {
        // O valor vem na posição 1 do array e como string, então convertemos para decimal
        setRam(parseFloat(ramData.data.result[0].value[1]));
      }

      // Busca métrica de CPU
      const cpuRes = await fetch(`http://localhost:8080/api/devices/${deviceId}/metrics?type=cpu`);
      const cpuData = await cpuRes.json();
      if (cpuData.data?.result?.length > 0) {
        setCpu(parseFloat(cpuData.data.result[0].value[1]));
      }
    } catch (error) {
      console.error(`Erro ao buscar métricas do device ${deviceId}:`, error);
    }
  };

  useEffect(() => {
    fetchMetrics(); // Busca imediata ao carregar a tela
    const interval = setInterval(fetchMetrics, 10000); // Atualiza a cada 10 segundos
    
    return () => clearInterval(interval); // Limpa o agendamento se o componente for destruído
  }, [deviceId]);

  return (
    <div className="mt-6 space-y-4">
      {/* Bloco da CPU */}
      <div>
        <div className="flex justify-between items-center mb-2">
          <Text>CPU Uso</Text>
          <Text>{cpu.toFixed(1)}%</Text>
        </div>
        <ProgressBar value={cpu} color="blue" />
      </div>

      {/* Bloco da RAM */}
      <div>
        <div className="flex justify-between items-center mb-2">
          <Text>RAM Uso</Text>
          <Text>{ram.toFixed(1)}%</Text>
        </div>
        <ProgressBar value={ram} color="emerald" />
      </div>
    </div>
  );
}