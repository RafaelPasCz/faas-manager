import { useEffect, useState } from 'react';
import { Text, Metric, Grid, TextInput, Select, SelectItem, Button } from '@tremor/react';
import AddDeviceModal from './AddDeviceModal';
import DeviceCard from './DeviceCard';

function App() {
  const [devices, setDevices] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterFunction, setFilterFunction] = useState(false);
  const [filterPrometheus, setFilterPrometheus] = useState(false);
  const [sortOrder, setSortOrder] = useState('newest');

  // Lógica do Dark Mode
  const [isDarkMode, setIsDarkMode] = useState(() => localStorage.getItem('theme') === 'dark');

  useEffect(() => {
    if (isDarkMode) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }, [isDarkMode]);

  const fetchDevices = () => {
    fetch('http://localhost:8080/api/devices')
      .then((response) => response.json())
      .then((data) => {
        if (Array.isArray(data)) setDevices(data);
      })
      .catch((error) => console.error('Erro de conexão:', error));
  };

  useEffect(() => {
    fetchDevices();
  }, []);

  const filteredDevices = devices
    .filter(d => {
      const term = searchTerm.toLowerCase();
      const matchesSearch = d.name.toLowerCase().includes(term) || (d.ipAddress && d.ipAddress.includes(term));
      const matchesFunc = filterFunction ? d.isFunctionHost : true;
      const matchesProm = filterPrometheus ? d.isPrometheusHost : true;
      return matchesSearch && matchesFunc && matchesProm;
    })
    .sort((a, b) => {
      const dateA = a.savedAt ? new Date(a.savedAt).getTime() : a.id;
      const dateB = b.savedAt ? new Date(b.savedAt).getTime() : b.id;
      return sortOrder === 'newest' ? dateB - dateA : dateA - dateB;
    });

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-zinc-900 transition-colors duration-300">
      <main className="p-10 max-w-7xl mx-auto text-slate-800 dark:text-gray-200">
        
        {/* Cabeçalho */}
        <div className="mb-8 flex justify-between items-end">
          <div>
            <Metric className="text-3xl dark:text-white">FaaS Manager Control Plane</Metric>
            <Text className="dark:text-zinc-400">Visão geral da topologia Edge/Fog.</Text>
          </div>
          <div className="flex gap-4">
            <Button 
              variant="secondary" 
              onClick={() => setIsDarkMode(!isDarkMode)}
              className="dark:bg-zinc-800 dark:text-white dark:border-zinc-700 dark:hover:bg-zinc-700"
            >
              {isDarkMode ? '☀️ Claro' : '🌙 Escuro'}
            </Button>

            <Button 
              onClick={() => setIsModalOpen(true)}
              className="bg-emerald-600 text-white hover:bg-emerald-700 dark:bg-emerald-500 dark:hover:bg-emerald-400 dark:text-zinc-900 border-transparent shadow-md transition-colors"
            >
              + Novo Dispositivo
            </Button>
          </div>
        </div>

        {/* Barra de Ferramentas */}
        <div className="mb-8 bg-white dark:bg-zinc-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-zinc-700 flex flex-wrap gap-4 items-center justify-between transition-colors">
          <div className="flex items-center gap-4 flex-1 min-w-[300px]">
            <TextInput 
              placeholder="Buscar por Nome ou IP..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="max-w-sm"
            />
            <label className="flex items-center gap-2 text-sm text-gray-600 dark:text-zinc-300 cursor-pointer hover:text-emerald-600 dark:hover:text-emerald-400 transition-colors">
              <input type="checkbox" checked={filterFunction} onChange={(e) => setFilterFunction(e.target.checked)} className="rounded text-emerald-600 focus:ring-emerald-500" />
              Hosts de Função
            </label>
            <label className="flex items-center gap-2 text-sm text-gray-600 dark:text-zinc-300 cursor-pointer hover:text-emerald-600 dark:hover:text-emerald-400 transition-colors">
              <input type="checkbox" checked={filterPrometheus} onChange={(e) => setFilterPrometheus(e.target.checked)} className="rounded text-emerald-600 focus:ring-emerald-500" />
              Hosts Prometheus
            </label>
          </div>
          <div className="w-48">
            <Select value={sortOrder} onValueChange={setSortOrder}>
              <SelectItem value="newest">Mais Recentes</SelectItem>
              <SelectItem value="oldest">Mais Antigos</SelectItem>
            </Select>
          </div>
        </div>

{/* Grid de Dispositivos */}
        <Grid numItemsSm={1} numItemsLg={3} className="gap-6 items-start">
          {filteredDevices.map((device) => (
            <DeviceCard 
              key={device.id} 
              device={device} 
              allDevices={devices} 
              onDeviceDeleted={fetchDevices} // <-- Adicione esta linha
            />
          ))}
        </Grid>

        {/* Modal */}
        <AddDeviceModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onDeviceAdded={fetchDevices} devices={devices} />

      </main>
    </div>
  );
}

export default App;