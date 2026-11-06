package com.portfolio.faasmanager.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.portfolio.faasmanager.exception.AttributeException;
import com.portfolio.faasmanager.exception.ResourceNotFoundException;
import com.portfolio.faasmanager.model.Device;
import com.portfolio.faasmanager.repository.DeviceRepository;

@Service
public class PrometheusService {

    private final DeviceRepository deviceRepository;
    private final RestTemplate restTemplate;
    
 

 
    public PrometheusService(DeviceRepository deviceRepository, RestTemplate restTemplate) {
        this.deviceRepository = deviceRepository;
        this.restTemplate = restTemplate;
    }

    public String getMetricFromDevice(Long deviceId, String type) {
            
        Device targetDevice = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado."));

        String prometheusUrl = resolvePrometheusUrl(targetDevice);
        String instanceFilter = targetDevice.getIpAddress() + ":9100";
        String promQlQuery = buildPromQlQuery(type, instanceFilter);

        try {
            String requestUrl = prometheusUrl + "/api/v1/query?query={query}";
            
            return restTemplate.getForObject(requestUrl, String.class, promQlQuery);
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar o Prometheus: " + e.getMessage());
        }
    }

    private String resolvePrometheusUrl(Device device) {
        if (device.getIsPrometheusHost()) {
            if (device.getPrometheusUrl() == null || device.getPrometheusUrl().isBlank()) {
                throw new AttributeException("O dispositivo está marcado como host Prometheus, mas a URL está vazia.");
            }
            return device.getPrometheusUrl();
        } 
        
        if (device.getPrometheusHostId() != null) {
            Device prometheusHost = deviceRepository.findById(device.getPrometheusHostId())
                .orElseThrow(() -> new ResourceNotFoundException("Host Prometheus associado não encontrado."));
            return prometheusHost.getPrometheusUrl();
        }

        throw new AttributeException("O dispositivo não possui um host Prometheus associado para coletar métricas.");
    }

    private String buildPromQlQuery(String type, String instance) {
        switch (type.toLowerCase()) {
            case "cpu":
                return String.format(
                    "100 - (avg by (instance) (rate(node_cpu_seconds_total{mode='idle', instance='%s'}[1m])) * 100)", 
                    instance
                );
            case "ram":
                return String.format(
                    "100 * (1 - (node_memory_MemAvailable_bytes{instance='%s'} / node_memory_MemTotal_bytes{instance='%s'}))",
                    instance, instance
                );
            default:
                throw new AttributeException("Tipo de métrica não suportado. Use 'cpu' ou 'ram'.");
        }
    }
}