package com.portfolio.faasmanager.controller;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.portfolio.faasmanager.exception.AttributeException;
import com.portfolio.faasmanager.exception.ResourceCreationException;
import com.portfolio.faasmanager.exception.ResourceDeleteException;
import com.portfolio.faasmanager.exception.ResourceEditException;
import com.portfolio.faasmanager.exception.ResourceNotFoundException;
import com.portfolio.faasmanager.model.Device;
import com.portfolio.faasmanager.repository.DeviceRepository;
import com.portfolio.faasmanager.service.PrometheusService;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "http://localhost:4200")
public class DeviceController {
    private final DeviceRepository repository;
    private final PrometheusService prometheusService;

    public DeviceController(DeviceRepository repository, PrometheusService prometheusService){
        this.repository = repository;
        this.prometheusService = prometheusService;
    }

    @PutMapping("/{id}/update-name")
    public ResponseEntity<Device> updateDeviceName(@PathVariable Long id, @RequestParam String newName){
        
        if (newName == null || newName.trim().isEmpty()) {
            throw new AttributeException("O novo nome deve ser informado e não pode ser vazio");
        }

        return repository.findById(id)
            .map(device -> {
                device.setName(newName);
                Device updated = repository.save(device);
                return ResponseEntity.ok(updated);
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar o dispositivo com o id: " + id));
    }

    @PutMapping("/{id}/update-prometheus-url")
    public ResponseEntity<Device> updateDevicePromerheusUrl(@PathVariable Long id, @RequestParam String newPrometheusUrl){

        if(newPrometheusUrl == null || newPrometheusUrl.strip() == ""){
            throw new AttributeException("A nova Url não pode ser nula");
        }
            

        return repository.findById(id)
            .map(device -> {
                device.setPrometheusUrl(newPrometheusUrl);
                Device updated = repository.save(device);
                return ResponseEntity.ok(updated);
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar o dispositivo com id: " + id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device deviceDetails) {
        if (deviceDetails.getName() == null) 
            throw new AttributeException("O nome não deve ser nulo");
        
        if (id == null) 
            throw new AttributeException("O Id do dispositivo deve ser informado");
        
        
        return repository.findById(id)
            .map(device -> {
                device.setName(deviceDetails.getName());
                device.setPrometheusUrl(deviceDetails.getPrometheusUrl());
                Device updated = repository.save(device);
                return ResponseEntity.ok(updated);
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar o dispoositivo: " + id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        if (id == null) 
            throw new AttributeException("O id não deve ser nulo");
        
        return repository.findById(id)
            .map(device -> {
                repository.delete(device); 
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseThrow(() -> new ResourceDeleteException("Erro ao deletar o dispositivo: " + id));
    }

    @DeleteMapping("/{id}/delete-prometheus-url")
    public ResponseEntity<Void> deletePrometheusUrl(@PathVariable Long id) {
        if (id == null) 
            throw new AttributeException("O id não deve ser nulo");
        
        return repository.findById(id)
        .map(device -> {
            device.setPrometheusUrl(null);
            repository.save(device);
            return ResponseEntity.noContent().<Void>build();
        })
            .orElseThrow(() -> new ResourceDeleteException("Erro ao deletar a URL do dispositivo: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Device createNewDevice(@RequestBody Device device) {

        if (device.getName() == null || device.getName().isEmpty()){ 
            throw new AttributeException("O nome não deve ser nulo");
        }

        String ipv4Regex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";

        if (device.getIpAddress() != null && !device.getIpAddress().matches(ipv4Regex)) {
            throw new AttributeException("O endereço IP fornecido é inválido.");
        }


        try {
            return repository.save(device);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceCreationException("Já existe um dispositivo cadastrado com este nome ou URL do Prometheus.");
        } catch (Exception e) {
            throw new ResourceCreationException("Erro ao criar dispositivo: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<JsonNode> getDeviceMetrics(
            @PathVariable Long id, 
            @RequestParam String type) {
        
        JsonNode metrics = prometheusService.getMetricFromDevice(id, type);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/find-all")
    public List<Device> findAll(){
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> findById(@PathVariable Long id){
        if (id == null) {
            throw new AttributeException("O Id do dispositivo deve ser informado");
        }
        return repository.findById(id)
        .map(device -> ResponseEntity.ok(device))
        .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado"));
    }

    @GetMapping("/find-by-name")
    public List<Device> findByName(@RequestParam String name){
        return repository.findByName(name);
    }

    @GetMapping("/{id}/created-date")
    public ResponseEntity<Device> getCreationDate(@PathVariable Long id) {
        if (id == null) {
            throw new AttributeException("O Id do dispositivo deve ser informado");
        }
        
        return repository.findById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado"));
    }
    
    @GetMapping("/{id}/ip-address")
    public ResponseEntity<Device> getIpAddress(@PathVariable Long id) {
        if (id == null) {
            throw new AttributeException("O Id do dispositivo deve ser informado");
        }
        
        return repository.findById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado"));
    }
    
    @PutMapping("/{id}/update-ip")
    public ResponseEntity<Device> updateDeviceIp(@PathVariable Long id, @RequestParam String newIpAddress) {
        
        if (newIpAddress == null || newIpAddress.trim().isEmpty()) {
            throw new AttributeException("O novo IP deve ser informado");
        }
    
        String ipv4Regex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
    
        if (!newIpAddress.matches(ipv4Regex)) {
            throw new AttributeException("O endereço IP fornecido é inválido. Envie um IPv4 válido.");
        }
    
        return repository.findById(id)
            .map(device -> {
                device.setIpAddress(newIpAddress);
                Device updated = repository.save(device);
                return ResponseEntity.ok(updated);
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar o IP do dispositivo com o id: " + id));
    }

    
}