import { useEffect, useState } from 'react';
import { Card, Text, Metric, Grid, Badge } from '@tremor/react';
import DeviceMetrics from './DeviceMetrics';
import AddDeviceCard from './AddDeviceCard'; // <-- Importe aqui

function App() {
  const [devices, setDevices] = useState([]);

  // Isolamos a função de busca para poder reutilizá-la
  const fetchDevices = () => {
    fetch('http://localhost:8080/api/devices')
      .then((response) => response.json())
      .then((data) => {
        if (Array.isArray(data)) {
          setDevices(data);
        } else {
          setDevices([]);
        }
      })
      .catch((error) => console.error('Erro de conexão ou CORS:', error));
  };

  // Executa ao carregar a tela pela primeira vez
  useEffect(() => {
    fetchDevices();
  }, []);

  return (
    <main className="p-10 max-w-7xl mx-auto">
      <div className="mb-8">
        <Metric>FaaS Manager Control Plane</Metric>
        <Text>Visão geral da topologia Edge/Fog.</Text>
      </div>

      <Grid numItemsSm={1} numItemsLg={3} className="gap-6">
        
        {/* O card de formulário fica como o primeiro item da grade */}
        <AddDeviceCard onDeviceAdded={fetchDevices} devices={devices} />

        {/* Lista os dispositivos existentes */}
        {devices.map((device) => (
          <Card key={device.id} decoration="top" decorationColor={device.isFunctionHost ? "blue" : "emerald"}>
            <Text>{device.ipAddress}</Text>
            <Metric className="mt-2 text-2xl">{device.name}</Metric>
            
            <div className="mt-4 flex gap-2">
              {device.isPrometheusHost && (
                <Badge color="emerald">Prometheus Host</Badge>
              )}
              {device.isFunctionHost && (
                <Badge color="blue">Function Host</Badge>
              )}
            </div>

            <DeviceMetrics deviceId={device.id} />
          </Card>
        ))}
      </Grid>
    </main>
  );
}

export default App;