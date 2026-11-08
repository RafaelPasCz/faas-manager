package com.portfolio.faasmanager.controller;

import java.util.Optional;
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
        if (deviceDetails.getName() == null) throw new AttributeException("O nome não deve ser nulo");
        if (id == null) throw new AttributeException("O Id do dispositivo deve ser informado");
        
        if (deviceDetails.getIpAddress() != null && !deviceDetails.getIpAddress().trim().isEmpty()) {
            String ipv4Regex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
            if (!deviceDetails.getIpAddress().matches(ipv4Regex)) {
                throw new AttributeException("O endereço IP fornecido é inválido. Envie um IPv4 válido.");
            }
        }
        
        return repository.findById(id)
        .map(device -> {
            device.setName(deviceDetails.getName());
            device.setIpAddress(deviceDetails.getIpAddress());
            device.setPrometheusUrl(deviceDetails.getPrometheusUrl());
            device.setPrometheusHostId(deviceDetails.getPrometheusHostId());
            device.setIsPrometheusHost(deviceDetails.getIsPrometheusHost());
            device.setIsFunctionHost(deviceDetails.getIsFunctionHost());
            device.setNodeExporterUrl(deviceDetails.getNodeExporterUrl());
            device.setGatewayPort(deviceDetails.getGatewayPort());
            
            Device updated = repository.save(device);
            return ResponseEntity.ok(updated);
        }).orElseThrow(() -> new ResourceEditException("Erro ao editar o dispositivo: " + id));
    }

    @PutMapping("/{id}/update-gateway-port")
    public ResponseEntity<Device> updateGatewayPort(@PathVariable Long id, @RequestParam Integer gatewayPort) {
        if (gatewayPort == null) {
            throw new AttributeException("A porta do Gateway deve ser informada");
        }

        return repository.findById(id)
            .map(device -> {
                device.setGatewayPort(gatewayPort);
                Device updated = repository.save(device);
                return ResponseEntity.ok(updated);
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar a porta de gateway do dispositivo com id: " + id));
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
            throw new ResourceCreationException("Erro nos dados do cadastro: verifique se algum campo obrigatório ficou vazio ou se já existe um dispositivo com o nome escolhido.");
        } catch (Exception e) {
            throw new ResourceCreationException("Erro ao criar dispositivo: " + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}/metrics", produces = "application/json")
    public ResponseEntity<String> getDeviceMetrics(
            @PathVariable Long id, 
            @RequestParam String type) {
        
       
        String metrics = prometheusService.getMetricFromDevice(id, type);
        return ResponseEntity.ok(metrics);
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
    public Optional<Device> findByName(@RequestParam String name){
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

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        List<Device> devices = repository.findAll();
        return ResponseEntity.ok(devices);
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

    @PutMapping("/{id}/update-prometheus-host")
    public ResponseEntity<Device> updatePrometheusHost(
            @PathVariable Long id, 
            @RequestParam(required = false) Long prometheusHostId) {
        
        return repository.findById(id)
            .map(device -> {
                device.setPrometheusHostId(prometheusHostId);
                Device updated = repository.save(device);
                return ResponseEntity.ok(updated);
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar o host Prometheus do dispositivo: " + id));
    }


    @PutMapping("/{id}/update-is-prometheus-host")
    public ResponseEntity<Device> updateIsPrometheusHost(@PathVariable Long id, @RequestParam Boolean isPrometheusHost) {
        if (isPrometheusHost == null) {
            throw new AttributeException("O status de host do Prometheus deve ser informado (true ou false)");
        }
        
        return repository.findById(id)
            .map(device -> {
                device.setIsPrometheusHost(isPrometheusHost); 
                Device updated = repository.save(device);
                return ResponseEntity.ok(updated);
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar o status de Prometheus Host do dispositivo com id: " + id));
    }

    @PutMapping("/{id}/update-is-function-host")
    public ResponseEntity<Device> updateIsFunctionHost(@PathVariable Long id, @RequestParam Boolean isFunctionHost) {
        if (isFunctionHost == null) {
            throw new AttributeException("O status de host de Função deve ser informado (true ou false)");
        }
        
        return repository.findById(id)
            .map(device -> {
                device.setIsFunctionHost(isFunctionHost); 
                Device updated = repository.save(device);
                return ResponseEntity.ok(updated);
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar o status de Function Host do dispositivo com id: " + id));
    }

    @GetMapping("/{id}/is-prometheus-host")
    public ResponseEntity<Boolean> getIsPrometheusHost(@PathVariable Long id) {
        if (id == null) throw new AttributeException("O Id do dispositivo deve ser informado");
        
        return repository.findById(id)
            .map(device -> ResponseEntity.ok(device.getIsPrometheusHost())) 
            .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado"));
    }

    @GetMapping("/{id}/is-function-host")
    public ResponseEntity<Boolean> getIsFunctionHost(@PathVariable Long id) {
        if (id == null) throw new AttributeException("O Id do dispositivo deve ser informado");
        
        return repository.findById(id)
            .map(device -> ResponseEntity.ok(device.getIsFunctionHost()))
            .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado"));
    }


    @PutMapping("/{id}/update-node-exporter-url")
    public ResponseEntity<Device> updateNodeExporterUrl(@PathVariable Long id, @RequestParam String newNodeExporterUrl) {
        
        if (newNodeExporterUrl == null || newNodeExporterUrl.trim().isEmpty()) {
            throw new AttributeException("A nova URL do Node Exporter não pode ser vazia");
        }

        return repository.findById(id)
            .map(device -> {
                device.setNodeExporterUrl(newNodeExporterUrl);
                Device updated = repository.save(device);
                return ResponseEntity.ok(updated);
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar a URL do Node Exporter do dispositivo com id: " + id));
    }

    @DeleteMapping("/{id}/delete-node-exporter-url")
    public ResponseEntity<Void> deleteNodeExporterUrl(@PathVariable Long id) {
        if (id == null) {
            throw new AttributeException("O id não deve ser nulo");
        }
        
        return repository.findById(id)
            .map(device -> {
                device.setNodeExporterUrl(null);
                repository.save(device);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseThrow(() -> new ResourceDeleteException("Erro ao deletar a URL do Node Exporter do dispositivo: " + id));
    }

    
}