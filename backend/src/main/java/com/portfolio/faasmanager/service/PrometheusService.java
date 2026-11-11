package com.portfolio.faasmanager.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.portfolio.faasmanager.dto.DeviceMetricsDTO; // Certifique-se de importar o DTO que criamos
import com.portfolio.faasmanager.exception.AttributeException;
import com.portfolio.faasmanager.exception.ResourceNotFoundException;
import com.portfolio.faasmanager.model.Device;
import com.portfolio.faasmanager.repository.DeviceRepository;

@Service
public class PrometheusService {

    private final DeviceRepository deviceRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; 
 
    public PrometheusService(DeviceRepository deviceRepository, RestTemplate restTemplate) {
        this.deviceRepository = deviceRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public DeviceMetricsDTO getMetrics(Long deviceId) {
            
        Device targetDevice = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado."));

        String prometheusUrl = resolvePrometheusUrl(targetDevice);
        String instanceFilter = targetDevice.getIpAddress() + ":9100";

        String cpuQuery = buildPromQlQuery("cpu", instanceFilter);
        String ramQuery = buildPromQlQuery("ram", instanceFilter);

        Double cpuValue = fetchAndParseMetric(prometheusUrl, cpuQuery);
        Double ramValue = fetchAndParseMetric(prometheusUrl, ramQuery);

        return new DeviceMetricsDTO(cpuValue, ramValue);
    }

    private Double fetchAndParseMetric(String prometheusUrl, String promQlQuery) {
        try {
            String requestUrl = prometheusUrl + "/api/v1/query?query={query}";
            String jsonResponse = restTemplate.getForObject(requestUrl, String.class, promQlQuery);
            
            return extractValueFromJson(jsonResponse);
            
        } catch (Exception e) {
            System.err.println("Erro ao consultar o Prometheus: " + e.getMessage());
            return 0.0; 
        }
    }

    private Double extractValueFromJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode resultNode = root.path("data").path("result");
            
            if (resultNode.isArray() && resultNode.size() > 0) {
                JsonNode valueNode = resultNode.get(0).path("value");
                if (valueNode.isArray() && valueNode.size() > 1) {
                    return valueNode.get(1).asDouble();
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar JSON: " + e.getMessage());
        }
        return 0.0;
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
                throw new AttributeException("Tipo de métrica não suportado.");
        }
    }
}