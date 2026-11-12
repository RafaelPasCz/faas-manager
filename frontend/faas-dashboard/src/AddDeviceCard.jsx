import { useState } from 'react';
import { Card, Text, TextInput, Button, Metric, Divider } from '@tremor/react';


export default function AddDeviceCard({ onDeviceAdded, devices = [] }) {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '', ipAddress: '', isPrometheusHost: false, prometheusUrl: '',
    prometheusHostId: '', gatewayPort: '', isFunctionHost: false, nodeExporterUrl: ''
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    const payload = {
      name: formData.name, ipAddress: formData.ipAddress, isPrometheusHost: formData.isPrometheusHost,
      prometheusUrl: formData.prometheusUrl || null, isFunctionHost: formData.isFunctionHost,
      nodeExporterUrl: formData.nodeExporterUrl || null,
      prometheusHostId: formData.prometheusHostId ? parseInt(formData.prometheusHostId) : null,
      gatewayPort: formData.gatewayPort ? parseInt(formData.gatewayPort) : null,
    };

    try {
      const response = await fetch('http://localhost:8080/api/devices', {
        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload),
      });

      if (response.ok) {
        setFormData({ name: '', ipAddress: '', isPrometheusHost: false, prometheusUrl: '', prometheusHostId: '', gatewayPort: '', isFunctionHost: false, nodeExporterUrl: '' });
        onDeviceAdded(); 
      }
    } catch (error) {
      console.error('Erro:', error);
    } finally {
      setLoading(false);
    }
  };

  const prometheusHosts = devices.filter(d => d.isPrometheusHost);

  return (
    <Card decoration="top" decorationColor="emerald" className="flex flex-col justify-between h-full bg-white dark:bg-zinc-800 dark:border-zinc-700">
      <div>
        <Metric className="text-xl mb-1 dark:text-white">Adicionar Dispositivo</Metric>
        <Text className="mb-4 dark:text-zinc-400">Cadastre um novo nó na topologia.</Text>
        
        <form onSubmit={handleSubmit} className="space-y-3 text-sm">
          <div className="grid grid-cols-2 gap-2">
            <TextInput name="name" placeholder="Nome" value={formData.name} onChange={handleChange} required />
            <TextInput name="ipAddress" placeholder="IP" value={formData.ipAddress} onChange={handleChange} required />
          </div>

          <Divider className="dark:border-zinc-700 dark:text-zinc-400">Monitoramento</Divider>

          <label className="flex items-center space-x-2 text-gray-700 dark:text-zinc-300 cursor-pointer hover:text-emerald-500 transition-colors">
            <input type="checkbox" name="isFunctionHost" checked={formData.isFunctionHost} onChange={handleChange} className="w-4 h-4 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500" />
            <span>É Host de Função?</span>
          </label>
          {formData.isFunctionHost && (
            <TextInput name="nodeExporterUrl" placeholder="URL Node Exporter" value={formData.nodeExporterUrl} onChange={handleChange} />
          )}

          <label className="flex items-center space-x-2 text-gray-700 dark:text-zinc-300 mt-3 cursor-pointer hover:text-emerald-500 transition-colors">
            <input type="checkbox" name="isPrometheusHost" checked={formData.isPrometheusHost} onChange={handleChange} className="w-4 h-4 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500" />
            <span>É Servidor Prometheus?</span>
          </label>
          {formData.isPrometheusHost ? (
            <TextInput name="prometheusUrl" placeholder="URL Prometheus" value={formData.prometheusUrl} onChange={handleChange} />
          ) : (
            <select
              name="prometheusHostId"
              value={formData.prometheusHostId}
              onChange={handleChange}
              className="w-full border border-gray-200 dark:border-zinc-700 bg-white dark:bg-zinc-900 rounded-md p-2 text-gray-700 dark:text-zinc-200 focus:ring-2 focus:ring-emerald-500 outline-none"
            >
              <option value="">Vincular a um Servidor Prometheus</option>
              {prometheusHosts.map(host => (
                <option key={host.id} value={host.id}>{host.name} ({host.ipAddress})</option>
              ))}
            </select>
          )}

          <Divider className="dark:border-zinc-700 dark:text-zinc-400">Rede</Divider>
          <TextInput name="gatewayPort" type="number" placeholder="Porta do Gateway" value={formData.gatewayPort} onChange={handleChange} />
          
          <Button type="submit" loading={loading} className="w-full mt-4 bg-emerald-600 text-white hover:bg-emerald-700 dark:bg-emerald-500 dark:hover:bg-emerald-400 dark:text-zinc-900 border-transparent transition-colors">
            Salvar Dispositivo
          </Button>
        </form>
      </div>
    </Card>
  );
}