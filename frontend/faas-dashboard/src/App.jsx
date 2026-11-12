import { useEffect, useState } from 'react';
import { 
  Text, 
  Metric, 
  Grid, 
  TextInput, 
  Select, 
  SelectItem, 
  Button,  
  TabGroup, 
  TabList, 
  Tab, 
  TabPanels, 
  TabPanel
} from '@tremor/react';
import AddDeviceModal from './AddDeviceModal';
import DeviceCard from './DeviceCard';
import AddFunctionModal from './AddFunctionModal';
import FunctionCard from './FunctionCard';
import ExecutionLogsTab from './ExecutionLogsTab'; 

function App() {

  const [devices, setDevices] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterFunction, setFilterFunction] = useState(false);
  const [filterPrometheus, setFilterPrometheus] = useState(false);
  const [sortOrder, setSortOrder] = useState('newest');
  

  const [functions, setFunctions] = useState([]);
  const [isFunctionModalOpen, setIsFunctionModalOpen] = useState(false);
  const [functionSearchTerm, setFunctionSearchTerm] = useState('');

 
  const [selectedIndex, setSelectedIndex] = useState(0);

  
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

  const fetchFunctions = () => {
    fetch('http://localhost:8080/api/function/find-all')
      .then((response) => response.json())
      .then((data) => {
        if (Array.isArray(data)) {
          setFunctions(data.filter(f => f.active !== false));
        }
      })
      .catch((error) => console.error('Erro de conexão:', error));
  };

  useEffect(() => {
    fetchDevices();
    fetchFunctions();
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
            <Metric className="text-3xl dark:text-white">FaasManager Control Plane</Metric>
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
        
        {/* Navegação por Abas */}
        <TabGroup index={selectedIndex} onIndexChange={setSelectedIndex}>
          <TabList className="mt-4 mb-6 border-b border-gray-200 dark:border-zinc-800">
            <Tab className={`text-lg font-medium transition-colors ${selectedIndex === 0 ? 'text-emerald-600 dark:text-emerald-400 border-b-2 border-emerald-500 dark:border-emerald-400' : 'text-gray-500 dark:text-zinc-400'}`}>
              Dispositivos
            </Tab>
            <Tab className={`text-lg font-medium transition-colors ${selectedIndex === 1 ? 'text-emerald-600 dark:text-emerald-400 border-b-2 border-emerald-500 dark:border-emerald-400' : 'text-gray-500 dark:text-zinc-400'}`}>
              Funções
            </Tab>
            <Tab className={`text-lg font-medium transition-colors ${selectedIndex === 2 ? 'text-emerald-600 dark:text-emerald-400 border-b-2 border-emerald-500 dark:border-emerald-400' : 'text-gray-500 dark:text-zinc-400'}`}>
              Logs de Execução
            </Tab>
          </TabList>
          
          <TabPanels>
            
            {/* ========================================= */}
            {/* ABA 1: DISPOSITIVOS (Infraestrutura Física) */}
            {/* ========================================= */}
            <TabPanel>
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
                <select 
                    value={sortOrder} 
                    onChange={(e) => setSortOrder(e.target.value)}
                    className="w-full rounded-md border border-gray-300 dark:border-zinc-700 bg-white dark:bg-zinc-900 px-3 py-2 text-sm text-gray-900 dark:text-zinc-200 shadow-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500 transition-colors h-[38px]"
                  >
                    <option value="newest">Mais Recentes</option>
                    <option value="oldest">Mais Antigos</option>
                  </select>
                </div>
              </div>

              {/* Grid de Dispositivos */}
              <Grid numItemsSm={1} numItemsLg={3} className="gap-6 items-start">
                {filteredDevices.map((device) => (
                  <DeviceCard 
                    key={device.id} 
                    device={device} 
                    allDevices={devices} 
                    onDeviceDeleted={fetchDevices} 
                  />
                ))}
              </Grid>
            </TabPanel>

            {/* ========================================= */}
            {/* ABA 2: FUNÇÕES ABSTRATAS (Catálogo)       */}
            {/* ========================================= */}
            <TabPanel>
            <div className="mt-4 mb-6 flex flex-wrap gap-4 justify-between items-center bg-white dark:bg-zinc-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-zinc-700">
                
                <div className="flex-1 min-w-[250px]">
                  <TextInput 
                    placeholder="Buscar função por nome..." 
                    value={functionSearchTerm}
                    onChange={(e) => setFunctionSearchTerm(e.target.value)}
                    className="max-w-sm"
                  />
                </div>

                <div>
                  <Button 
                    onClick={() => setIsFunctionModalOpen(true)}
                    className="bg-emerald-600 text-white hover:bg-emerald-700 dark:bg-emerald-500 dark:hover:bg-emerald-400 dark:text-zinc-900 border-transparent shadow-md transition-colors"
                  >
                    + Nova Função
                  </Button>
                </div>
              </div>

              {functions.length === 0 ? (
                <div className="mt-8 text-center p-12 border border-dashed border-gray-300 dark:border-zinc-700 rounded-lg">
                  <Text className="text-gray-500 dark:text-zinc-500 text-lg">
                    Nenhuma função cadastrada. Clique no botão acima para adicionar.
                  </Text>
                </div>
              ) : (
                <Grid numItemsSm={1} numItemsLg={3} className="gap-6 items-start">
                  {functions
                    .filter(func => func.name.toLowerCase().includes(functionSearchTerm.toLowerCase()))
                    .map(func => (
                      <FunctionCard 
                        key={func.id} 
                        func={func} 
                        onFunctionDeleted={fetchFunctions} 
                      />
                  ))}
                </Grid>
              )}
            </TabPanel>

            {/* ========================================= */}
            {/* ABA 3: LOGS DE EXECUÇÃO DETALHADOS        */}
            {/* ========================================= */}
            <TabPanel>
              <ExecutionLogsTab />
            </TabPanel>

          </TabPanels>
        </TabGroup>

        <AddDeviceModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onDeviceAdded={fetchDevices} devices={devices} />
        <AddFunctionModal isOpen={isFunctionModalOpen} onClose={() => setIsFunctionModalOpen(false)} onFunctionAdded={fetchFunctions} />

      </main>
    </div> 
  );
}

export default App;