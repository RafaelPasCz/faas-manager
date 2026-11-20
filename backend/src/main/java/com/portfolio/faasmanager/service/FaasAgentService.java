package com.portfolio.faasmanager.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.faasmanager.exception.AttributeException;
import com.portfolio.faasmanager.exception.ResourceCreationException;
import com.portfolio.faasmanager.exception.ResourceDeleteException;
import com.portfolio.faasmanager.model.Device;
import com.portfolio.faasmanager.model.ExecutionLog;
import com.portfolio.faasmanager.model.Function;
import com.portfolio.faasmanager.model.FunctionInstance;
import com.portfolio.faasmanager.repository.DeviceRepository;
import com.portfolio.faasmanager.repository.ExecutionLogRepository;
import com.portfolio.faasmanager.repository.FunctionInstanceRepository;

@Service
public class FaasAgentService {

    private static final int DEFAULT_AGENT_PORT = 8081;

    private final DeviceRepository deviceRepository;
    private final FunctionInstanceRepository instanceRepository;
    private final ExecutionLogRepository executionLogRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<Long, Long> lastSyncedLogIdByDevice = new ConcurrentHashMap<>();

    @Value("${app.network.host-loopback-alias:}")
    private String hostLoopbackAlias;

    public FaasAgentService(
            DeviceRepository deviceRepository,
            FunctionInstanceRepository instanceRepository,
            ExecutionLogRepository executionLogRepository,
            RestTemplate restTemplate) {
        this.deviceRepository = deviceRepository;
        this.instanceRepository = instanceRepository;
        this.executionLogRepository = executionLogRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String deployToAgent(Device device, Function function) {
        String url = resolveAgentBaseUrl(device) + "/deploy";

        Map<String, Object> payload = new HashMap<>();
        payload.put("functionName", function.getName());
        payload.put("imageName", function.getDockerImage());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            if (response.getBody() != null && !response.getBody().isBlank()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return mapAgentStatus(root);
            }
            return "IDLE";
        } catch (HttpStatusCodeException e) {
            throw new ResourceCreationException(
                "Erro no faas-agent ao implantar função (" + e.getStatusCode() + "): " + e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            throw new ResourceCreationException(
                "Falha ao comunicar com o faas-agent (" + url + "): " + e.getMessage()
            );
        }
    }

    public void undeployFromAgent(Device device, Function function) {
        String url = resolveAgentBaseUrl(device) + "/function/" + function.getName();

        try {
            restTemplate.delete(url);
        } catch (HttpClientErrorException.NotFound ignored) {
            // Função já não está registrada na memória do agente; prossegue com a remoção no painel.
        } catch (HttpClientErrorException.Conflict e) {
            throw new ResourceDeleteException(
                "Não é possível remover a função no faas-agent enquanto há requisições ativas: " + e.getResponseBodyAsString()
            );
        } catch (HttpStatusCodeException e) {
            throw new ResourceDeleteException(
                "Erro no faas-agent ao remover função (" + e.getStatusCode() + "): " + e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            throw new ResourceDeleteException(
                "Falha ao comunicar com o faas-agent (" + url + "): " + e.getMessage()
            );
        }
    }

    public void syncInstances(List<FunctionInstance> instances) {
        if (instances == null || instances.isEmpty()) {
            return;
        }

        Map<Long, List<FunctionInstance>> byDevice = instances.stream()
            .filter(inst -> !Boolean.FALSE.equals(inst.getActive()) && inst.getDevice() != null)
            .collect(Collectors.groupingBy(inst -> inst.getDevice().getId()));

        for (List<FunctionInstance> deviceInstances : byDevice.values()) {
            syncDeviceInstances(deviceInstances.get(0).getDevice(), deviceInstances);
        }
    }

    public void syncDeviceInstances(Device device, List<FunctionInstance> instances) {
        if (device == null || !device.getIsFunctionHost() || Boolean.FALSE.equals(device.getActive())) {
            return;
        }
        if (device.getIpAddress() == null || device.getIpAddress().isBlank()) {
            return;
        }

        List<FunctionInstance> activeInstances = instances.stream()
            .filter(inst -> !Boolean.FALSE.equals(inst.getActive()))
            .collect(Collectors.toList());

        if (activeInstances.isEmpty()) {
            return;
        }

        String url = resolveAgentBaseUrl(device) + "/functions";

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            if (jsonResponse == null || jsonResponse.isBlank()) {
                return;
            }

            JsonNode arrayNode = objectMapper.readTree(jsonResponse);
            if (!arrayNode.isArray()) {
                return;
            }

            Map<String, String> agentStatusByName = new HashMap<>();
            for (JsonNode fnNode : arrayNode) {
                String fnName = fnNode.path("functionName").asText("");
                if (!fnName.isBlank()) {
                    agentStatusByName.put(fnName, mapAgentStatus(fnNode));
                }
            }

            List<FunctionInstance> changed = new ArrayList<>();
            for (FunctionInstance inst : activeInstances) {
                if (inst.getFunction() == null) {
                    continue;
                }
                String fnName = inst.getFunction().getName();
                String resolvedStatus = agentStatusByName.getOrDefault(fnName, "MISSING");
                if (!resolvedStatus.equals(inst.getStatus())) {
                    inst.setStatus(resolvedStatus);
                    changed.add(inst);
                }
            }

            if (!changed.isEmpty()) {
                instanceRepository.saveAll(changed);
            }
        } catch (Exception e) {
            System.err.println("Aviso ao sincronizar status das funções no faas-agent (" + url + "): " + e.getMessage());
        }
    }

    public void syncAllDevicesExecutionLogs() {
        List<Device> functionHosts = deviceRepository.findByIsFunctionHostTrue();
        for (Device host : functionHosts) {
            syncDeviceExecutionLogs(host);
        }
    }

    public void syncExecutionLogsByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        deviceRepository.findById(deviceId).ifPresent(this::syncDeviceExecutionLogs);
    }

    public void syncDeviceExecutionLogs(Device device) {
        if (device == null || !device.getIsFunctionHost() || Boolean.FALSE.equals(device.getActive())) {
            return;
        }
        if (device.getIpAddress() == null || device.getIpAddress().isBlank()) {
            return;
        }

        String baseUrl = resolveAgentBaseUrl(device);
        Long afterId = lastSyncedLogIdByDevice.get(device.getId());
        String url = afterId != null
            ? baseUrl + "/logs?afterId=" + afterId
            : baseUrl + "/logs";

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            if (jsonResponse == null || jsonResponse.isBlank()) {
                return;
            }

            JsonNode arrayNode = objectMapper.readTree(jsonResponse);
            if (!arrayNode.isArray() || arrayNode.isEmpty()) {
                return;
            }

            Map<String, FunctionInstance> instancesByFunctionName = mapDeviceInstancesByFunctionName(device.getId());
            List<ExecutionLog> logsToSave = new ArrayList<>();
            long maxSeenId = afterId != null ? afterId : 0L;

            for (JsonNode logNode : arrayNode) {
                long agentLogId = logNode.path("id").asLong(0L);
                if (agentLogId > maxSeenId) {
                    maxSeenId = agentLogId;
                }

                String functionName = logNode.path("functionName").asText("");
                FunctionInstance matchedInstance = instancesByFunctionName.get(functionName);
                if (matchedInstance == null) {
                    continue;
                }

                LocalDateTime executedAt = parseExecutedAt(logNode.path("executedAt").asText(""));
                if (agentLogId > 0 && executionLogRepository
                        .existsByFunctionInstance_Device_IdAndAgentLogIdAndExecutedAt(device.getId(), agentLogId, executedAt)) {
                    continue;
                }

                long durationMs = logNode.path("requestDurationMs").asLong(0L);
                int statusCode = logNode.path("statusCode").asInt(0);
                boolean success = logNode.path("success").asBoolean(false);

                ExecutionLog executionLog = new ExecutionLog(durationMs, success, matchedInstance);
                executionLog.setStatusCode(statusCode);
                executionLog.setExecutedAt(executedAt);
                if (agentLogId > 0) {
                    executionLog.setAgentLogId(agentLogId);
                }
                logsToSave.add(executionLog);
            }

            if (!logsToSave.isEmpty()) {
                executionLogRepository.saveAll(logsToSave);
            }
            if (maxSeenId > 0) {
                lastSyncedLogIdByDevice.put(device.getId(), maxSeenId);
            }
        } catch (Exception e) {
            System.err.println("Aviso ao coletar logs do faas-agent (" + url + "): " + e.getMessage());
        }
    }

    private Map<String, FunctionInstance> mapDeviceInstancesByFunctionName(Long deviceId) {
        List<FunctionInstance> deviceInstances = instanceRepository.findByDeviceId(deviceId);
        Map<String, FunctionInstance> map = new HashMap<>();
        for (FunctionInstance inst : deviceInstances) {
            if (inst.getFunction() == null || inst.getFunction().getName() == null) {
                continue;
            }
            String fnName = inst.getFunction().getName();
            if (!Boolean.FALSE.equals(inst.getActive()) || !map.containsKey(fnName)) {
                map.put(fnName, inst);
            }
        }
        return map;
    }

    private String mapAgentStatus(JsonNode fnNode) {
        String executionState = fnNode.path("executionState").asText("");
        int activeRequests = fnNode.path("activeRequests").asInt(0);
        if ("EXECUTING".equalsIgnoreCase(executionState) || activeRequests > 0) {
            return "EXECUTING";
        }

        String status = fnNode.path("status").asText("");
        if ("STOPPED".equalsIgnoreCase(status)) {
            return "STOPPED";
        }

        return "IDLE";
    }

    private LocalDateTime parseExecutedAt(String rawTimestamp) {
        if (rawTimestamp == null || rawTimestamp.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(rawTimestamp)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(rawTimestamp);
            } catch (Exception ignored) {
                return LocalDateTime.now();
            }
        }
    }

    private String resolveAgentBaseUrl(Device device) {
        if (device == null || device.getIpAddress() == null || device.getIpAddress().isBlank()) {
            throw new AttributeException("O dispositivo não possui endereço IP configurado para comunicar com o faas-agent.");
        }

        String host = device.getIpAddress().trim();
        int port = (device.getGatewayPort() != null && device.getGatewayPort() > 0)
            ? device.getGatewayPort()
            : DEFAULT_AGENT_PORT;

        String rawUrl = host.startsWith("http://") || host.startsWith("https://")
            ? host.replaceAll("/+$", "")
            : "http://" + host + ":" + port;

        return rewriteLoopbackIfContainerized(rawUrl);
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
}
