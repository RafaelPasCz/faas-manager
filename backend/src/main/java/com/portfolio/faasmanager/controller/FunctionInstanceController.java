package com.portfolio.faasmanager.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.portfolio.faasmanager.exception.*;
import com.portfolio.faasmanager.model.Device;
import com.portfolio.faasmanager.model.Function;
import com.portfolio.faasmanager.model.FunctionInstance;
import com.portfolio.faasmanager.repository.DeviceRepository;
import com.portfolio.faasmanager.repository.FunctionInstanceRepository;
import com.portfolio.faasmanager.repository.FunctionRepository;
import com.portfolio.faasmanager.service.FaasAgentService;

@RestController
@RequestMapping("/api/function-instances")
public class FunctionInstanceController {
    
    private final FunctionInstanceRepository instanceRepository;
    private final FunctionRepository functionRepository;
    private final DeviceRepository deviceRepository;
    private final FaasAgentService faasAgentService;

    public FunctionInstanceController(FunctionInstanceRepository instanceRepository, 
                                      FunctionRepository functionRepository, 
                                      DeviceRepository deviceRepository,
                                      FaasAgentService faasAgentService) {
        this.instanceRepository = instanceRepository;
        this.functionRepository = functionRepository;
        this.deviceRepository = deviceRepository;
        this.faasAgentService = faasAgentService;
    }

    @PostMapping("/deploy")
    @ResponseStatus(HttpStatus.CREATED)
    public FunctionInstance deployFunction(@RequestParam Long functionId, @RequestParam Long deviceId) {
        
        Function function = functionRepository.findById(functionId)
            .orElseThrow(() -> new ResourceNotFoundException("Função não encontrada."));
            
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado."));

        if (!device.getIsFunctionHost()) {
            throw new AttributeException("O dispositivo não é um host de função válido.");
        }

        Optional<FunctionInstance> activeInstanceOpt = instanceRepository
            .findFirstByFunctionIdAndDeviceIdAndActiveTrue(functionId, deviceId);

        FunctionInstance instance;
        if (activeInstanceOpt.isPresent()) {
            FunctionInstance existing = activeInstanceOpt.get();
            if (!"MISSING".equalsIgnoreCase(existing.getStatus()) && !"ERROR".equalsIgnoreCase(existing.getStatus())) {
                throw new ResourceConflictException("Conflito: A função selecionada já está provisionada neste dispositivo.");
            }
            instance = existing;
        } else {
            instance = new FunctionInstance();
            instance.setFunction(function);
            instance.setDevice(device);
        }

        instance.setActive(true);
        instance.setStatus("DEPLOYING");

        String agentStatus = faasAgentService.deployToAgent(device, function);
        instance.setStatus(agentStatus);

        return instanceRepository.save(instance);
    }

    @PostMapping("/{id}/redeploy")
    public ResponseEntity<FunctionInstance> redeployFunctionInstance(@PathVariable Long id) {
        FunctionInstance instance = instanceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Instância não encontrada."));

        Device device = instance.getDevice();
        Function function = instance.getFunction();

        if (device == null || !device.getIsFunctionHost()) {
            throw new AttributeException("O dispositivo associado não é um host de função válido.");
        }

        String agentStatus = faasAgentService.deployToAgent(device, function);
        instance.setActive(true);
        instance.setStatus(agentStatus);

        return ResponseEntity.ok(instanceRepository.save(instance));
    }

    @GetMapping("/find-all")
    public List<FunctionInstance> findAll() {
        List<FunctionInstance> instances = instanceRepository.findAll();
        faasAgentService.syncInstances(instances);
        return instances;
    }

    @GetMapping("/status/{status}")
    public List<FunctionInstance> findByStatus(@PathVariable String status) {
        return instanceRepository.findByStatus(status);
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<FunctionInstance> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return instanceRepository.findById(id)
            .map(instance -> {
                instance.setStatus(status);
                return ResponseEntity.ok(instanceRepository.save(instance));
            }).orElseThrow(() -> new ResourceEditException("Instância não encontrada."));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunctionInstance(@PathVariable Long id) {
        if (id == null) throw new AttributeException("O id não deve ser nulo");
        
        return instanceRepository.findById(id)
            .map(instance -> {
                faasAgentService.undeployFromAgent(instance.getDevice(), instance.getFunction());
                instance.setActive(false);
                instance.setStatus("STOPPED");
                instanceRepository.save(instance); 
                
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseThrow(() -> new ResourceDeleteException("Erro ao deletar a instância: " + id));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<FunctionInstance>> findByDeviceId(@PathVariable Long deviceId) {
        if (deviceId == null) {
            throw new AttributeException("O Id do dispositivo deve ser informado");
        }
        
        List<FunctionInstance> instances = instanceRepository.findByDeviceId(deviceId);
        deviceRepository.findById(deviceId)
            .ifPresent(device -> faasAgentService.syncDeviceInstances(device, instances));
        return ResponseEntity.ok(instances);
    }

    @GetMapping("/function/{functionId}")
    public ResponseEntity<List<FunctionInstance>> findByFunctionId(@PathVariable Long functionId){
        if (functionId == null){
            throw new AttributeException("o Id da função deve ser informao");
        }
        
        List<FunctionInstance> instances = instanceRepository.findByFunctionId(functionId);
        faasAgentService.syncInstances(instances);
        return ResponseEntity.ok(instances);
    };
}
