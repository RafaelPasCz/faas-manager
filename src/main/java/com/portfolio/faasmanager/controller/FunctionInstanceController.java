package com.portfolio.faasmanager.controller;

import java.util.List;
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

@RestController
@RequestMapping("/api/function-instances")
@CrossOrigin(origins = "http://localhost:4200")
public class FunctionInstanceController {
    
    private final FunctionInstanceRepository instanceRepository;
    private final FunctionRepository functionRepository;
    private final DeviceRepository deviceRepository;

    public FunctionInstanceController(FunctionInstanceRepository instanceRepository, 
                                      FunctionRepository functionRepository, 
                                      DeviceRepository deviceRepository) {
        this.instanceRepository = instanceRepository;
        this.functionRepository = functionRepository;
        this.deviceRepository = deviceRepository;
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


        if (instanceRepository.findByFunctionIdAndDeviceId(functionId, deviceId).isPresent()) {
            throw new ResourceConflictException("Conflito: A função selecionada já está provisionada neste dispositivo.");
        }

        FunctionInstance instance = new FunctionInstance();
        instance.setFunction(function);
        instance.setDevice(device);
        instance.setStatus("DEPLOYING"); 
        
        // TODO: Aqui entraria a chamada HTTP Push para o faas-agent baixar a imagem Docker
        // Após a confirmação do agente, o status mudaria para "IDLE".

        return instanceRepository.save(instance);
    }

    @GetMapping("/find-all")
    public List<FunctionInstance> findAll() {
        return instanceRepository.findAll();
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
    public ResponseEntity<Void> tearDown(@PathVariable Long id) {
        return instanceRepository.findById(id)
            .map(instance -> {
                // TODO: Chamada para o faas-agent dar docker rm
                instanceRepository.delete(instance);
                return ResponseEntity.noContent().<Void>build();
            }).orElseThrow(() -> new ResourceNotFoundException("Instância não encontrada."));
    }
}