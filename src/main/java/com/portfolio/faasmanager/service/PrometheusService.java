package com.portfolio.faasmanager.service;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.JsonNode;

import com.portfolio.faasmanager.model.Device;
import com.portfolio.faasmanager.repository.DeviceRepository;
import com.portfolio.faasmanager.exception.ResourceNotFoundException;
import com.portfolio.faasmanager.exception.GeneralException;

@Service
public class PrometheusService {
    private final DeviceRepository deviceRepository;
    private final RestTemplate restTemplate;

    public PrometheusService(DeviceRepository deviceRepository){
        this.deviceRepository = deviceRepository;
        this.restTemplate = new RestTemplate();
    }

    public JsonNode getMetricFromDevice(Long deviceId, String type){
        Device device = deviceRepository.findById(deviceId)
        .orElseThrow(() -> new ResourceNotFoundException("Dispositivo com id " + deviceId + " Não encontrado" ));

        if(device.getPrometheusUrl() == null || device.getPrometheusUrl().strip().isEmpty()){
            throw new GeneralException("Url do prometheus para o dispositivo com id " + deviceId + " não encontrado");
        }

        String prometheusApiUrl = device.getPrometheusUrl() + "/api/v1/query?query=";

        String promQlQuery = "";
        if (type.equals("cpu")) {
            promQlQuery = "(1 - avg by (instance) (rate(node_cpu_seconds_total {mode=\"idle\"}[15s]))) ";

        } else if (type.equals("ram")) {
            promQlQuery = "((avg_over_time(node_memory_MemTotal_bytes[15s]) - avg_over_time(node_memory_MemAvailable_bytes[15s]))" + 
            "/ avg_over_time(node_memory_MemTotal_bytes[15s])) * 100"; 

        } else {
            throw new GeneralException("Tipo de métrica não suportado: " + type);
        }

        try {
            String fullUrl = prometheusApiUrl + promQlQuery;
            return restTemplate.getForObject(fullUrl, JsonNode.class);
        } catch (RestClientException e) {
            throw new GeneralException("Erro ao se comunicar com o Prometheus do dispositivo: " + e.getMessage());
        }
    }

 }
