package com.portfolio.faasmanager.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.portfolio.faasmanager.dto.DeviceMetricsDTO;
import com.portfolio.faasmanager.exception.AttributeException;
import com.portfolio.faasmanager.exception.ResourceNotFoundException;
import com.portfolio.faasmanager.model.Device;
import com.portfolio.faasmanager.repository.DeviceRepository;

@Service
public class PrometheusService {

    private final DeviceRepository deviceRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.network.host-loopback-alias:}")
    private String hostLoopbackAlias;

    public PrometheusService(DeviceRepository deviceRepository, RestTemplate restTemplate) {
        this.deviceRepository = deviceRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public DeviceMetricsDTO getMetrics(Long deviceId) {
        Device targetDevice = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado."));

        String prometheusUrl = resolvePrometheusUrl(targetDevice);
        List<String> instanceCandidates = buildInstanceCandidates(targetDevice);

        Double cpuValue = null;
        Double ramValue = null;

        for (String instanceFilter : instanceCandidates) {
            String cpuQuery = buildPromQlQuery("cpu", instanceFilter);
            String ramQuery = buildPromQlQuery("ram", instanceFilter);

            Double candidateCpu = fetchAndParseMetric(prometheusUrl, cpuQuery);
            Double candidateRam = fetchAndParseMetric(prometheusUrl, ramQuery);

            if (candidateCpu != null || candidateRam != null) {
                cpuValue = candidateCpu != null ? candidateCpu : 0.0;
                ramValue = candidateRam != null ? candidateRam : 0.0;
                break;
            }
        }

        return new DeviceMetricsDTO(
            cpuValue != null ? cpuValue : 0.0,
            ramValue != null ? ramValue : 0.0
        );
    }

    private List<String> buildInstanceCandidates(Device device) {
        Set<String> candidates = new LinkedHashSet<>();

        if (device.getIpAddress() != null && !device.getIpAddress().isBlank()) {
            String ip = device.getIpAddress().trim();
            candidates.add(ip.contains(":") ? ip : ip + ":9100");
        }

        if (device.getNodeExporterUrl() != null && !device.getNodeExporterUrl().isBlank()) {
            try {
                URI uri = URI.create(device.getNodeExporterUrl().trim());
                if (uri.getHost() != null) {
                    int port = uri.getPort() != -1 ? uri.getPort() : 9100;
                    candidates.add(uri.getHost() + ":" + port);
                }
            } catch (Exception ignored) {
            }
        }

        candidates.add("localhost:9100");
        candidates.add("127.0.0.1:9100");

        return new ArrayList<>(candidates);
    }

    private Double fetchAndParseMetric(String prometheusUrl, String promQlQuery) {
        try {
            String requestUrl = prometheusUrl + "/api/v1/query?query={query}";
            String jsonResponse = restTemplate.getForObject(requestUrl, String.class, promQlQuery);

            return extractValueFromJson(jsonResponse);
        } catch (Exception e) {
            System.err.println("Erro ao consultar o Prometheus (" + prometheusUrl + "): " + e.getMessage());
            return null;
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
        return null;
    }

    private String resolvePrometheusUrl(Device device) {
        String rawUrl = null;

        if (device.getIsPrometheusHost()) {
            if (device.getPrometheusUrl() == null || device.getPrometheusUrl().isBlank()) {
                throw new AttributeException("O dispositivo está marcado como host Prometheus, mas a URL está vazia.");
            }
            rawUrl = device.getPrometheusUrl();
        } else if (device.getPrometheusHostId() != null) {
            Device prometheusHost = deviceRepository.findById(device.getPrometheusHostId())
                .orElseThrow(() -> new ResourceNotFoundException("Host Prometheus associado não encontrado."));
            rawUrl = prometheusHost.getPrometheusUrl();
        }

        if (rawUrl == null || rawUrl.isBlank()) {
            throw new AttributeException("O dispositivo não possui um host Prometheus associado para coletar métricas.");
        }

        return rewriteLoopbackIfContainerized(rawUrl.trim().replaceAll("/+$", ""));
    }

    private String rewriteLoopbackIfContainerized(String url) {
        if (hostLoopbackAlias == null || hostLoopbackAlias.isBlank()) {
            return url;
        }
        return url.replaceFirst(
            "^(https?://)(localhost|127\\.0\\.0\\.1)(?=(:\\d+|/|$))",
            "$1" + hostLoopbackAlias.trim()
        );
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
