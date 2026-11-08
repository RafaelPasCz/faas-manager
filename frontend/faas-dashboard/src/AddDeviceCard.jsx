import { useState } from 'react';
import { Card, Text, TextInput, Button, Metric, Divider } from '@tremor/react';

export default function AddDeviceCard({ onDeviceAdded, devices = [] }) {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    ipAddress: '',
    isPrometheusHost: false,
    prometheusUrl: '',
    prometheusHostId: '',
    gatewayPort: '',
    isFunctionHost: false,
    nodeExporterUrl: ''
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    // Formata os tipos de dados exatamente como o backend Java espera
    const payload = {
      name: formData.name,
      ipAddress: formData.ipAddress,
      isPrometheusHost: formData.isPrometheusHost,
      prometheusUrl: formData.prometheusUrl || null,
      isFunctionHost: formData.isFunctionHost,
      nodeExporterUrl: formData.nodeExporterUrl || null,
      // Converte strings para números caso estejam preenchidos
      prometheusHostId: formData.prometheusHostId ? parseInt(formData.prometheusHostId) : null,
      gatewayPort: formData.gatewayPort ? parseInt(formData.gatewayPort) : null,
    };

    try {
      const response = await fetch('http://localhost:8080/api/devices', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        // Limpa o formulário após o sucesso
        setFormData({
          name: '', ipAddress: '', isPrometheusHost: false, prometheusUrl: '',
          prometheusHostId: '', gatewayPort: '', isFunctionHost: false, nodeExporterUrl: ''
        });
        onDeviceAdded(); // Atualiza a lista na tela principal
      } else {
        console.error('Erro ao cadastrar dispositivo', await response.text());
      }
    } catch (error) {
      console.error('Erro de conexão:', error);
    } finally {
      setLoading(false);
    }
  };

  // Filtra a lista de dispositivos para mostrar no Select apenas os que são Prometheus
  const prometheusHosts = devices.filter(d => d.isPrometheusHost);

  return (
    <Card decoration="top" decorationColor="amber" className="flex flex-col justify-between">
      <div>
        <Metric className="text-xl mb-1">Adicionar Dispositivo</Metric>
        <Text className="mb-4">Cadastre um novo nó na topologia.</Text>
        
        <form onSubmit={handleSubmit} className="space-y-3 text-sm">
          {/* Dados Básicos */}
          <div className="grid grid-cols-2 gap-2">
            <TextInput name="name" placeholder="Nome (ex: edge-node-1)" value={formData.name} onChange={handleChange} required />
            <TextInput name="ipAddress" placeholder="IP (ex: 192.168.1.10)" value={formData.ipAddress} onChange={handleChange} required />
          </div>

          <Divider>Configuração de Monitoramento</Divider>

          {/* Node Exporter */}
          <label className="flex items-center space-x-2 text-gray-700">
            <input type="checkbox" name="isFunctionHost" checked={formData.isFunctionHost} onChange={handleChange} className="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
            <span>É Host de Função (Node Exporter)?</span>
          </label>
          {formData.isFunctionHost && (
            <TextInput name="nodeExporterUrl" placeholder="URL Node Exporter (ex: http://ip:9100/metrics)" value={formData.nodeExporterUrl} onChange={handleChange} />
          )}

          {/* Prometheus */}
          <label className="flex items-center space-x-2 text-gray-700 mt-3">
            <input type="checkbox" name="isPrometheusHost" checked={formData.isPrometheusHost} onChange={handleChange} className="w-4 h-4 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500" />
            <span>É Servidor Prometheus?</span>
          </label>
          {formData.isPrometheusHost ? (
            <TextInput name="prometheusUrl" placeholder="URL Prometheus (ex: http://ip:9090)" value={formData.prometheusUrl} onChange={handleChange} />
          ) : (
            <select
              name="prometheusHostId"
              value={formData.prometheusHostId}
              onChange={handleChange}
              className="w-full border border-gray-200 rounded-md p-2 text-gray-700 focus:ring-2 focus:ring-blue-500 outline-none"
            >
              <option value="">Vincular a um Servidor Prometheus (Opcional)</option>
              {prometheusHosts.map(host => (
                <option key={host.id} value={host.id}>{host.name} ({host.ipAddress})</option>
              ))}
            </select>
          )}

          <Divider>Rede</Divider>

          <TextInput name="gatewayPort" type="number" placeholder="Porta do Gateway (Opcional)" value={formData.gatewayPort} onChange={handleChange} />
          
          <Button type="submit" loading={loading} className="w-full mt-4 bg-gray-200 text-black hover:bg-gray-300 border-transparent">
            Salvar Dispositivo
          </Button>
        </form>
      </div>
    </Card>
  );
}