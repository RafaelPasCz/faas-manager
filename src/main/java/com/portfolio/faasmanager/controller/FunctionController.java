package com.portfolio.faasmanager.controller;

import java.util.List;

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
import com.portfolio.faasmanager.exception.ResourceEditException;
import com.portfolio.faasmanager.exception.ResourceNotFoundException;
import com.portfolio.faasmanager.model.Device;
import com.portfolio.faasmanager.model.Function;
import com.portfolio.faasmanager.repository.DeviceRepository;
import com.portfolio.faasmanager.repository.FunctionRepository;


@RestController
@RequestMapping("/api/function")
@CrossOrigin(origins = "http://localhost:4200")

public class FunctionController {
    private final FunctionRepository functionRepository;
    private final DeviceRepository deviceRepository;

    public FunctionController(FunctionRepository functionRepository, DeviceRepository deviceRepository){
        this.functionRepository = functionRepository;
        this.deviceRepository = deviceRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Function createFunction(@RequestBody Function function) {
        
        if (function.getDevice() == null || function.getDevice().getId() == null) {
            throw new AttributeException("A função deve estar vinculada ao ID de um dispositivo existente.");
        }


        Device targetDevice = deviceRepository.findById(function.getDevice().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Dispositivo alvo não encontrado no cluster."));


        if (!targetDevice.getIsFunctionHost()) {
            throw new AttributeException("Operação negada: O dispositivo selecionado (ID: " 
                + targetDevice.getId() + ") não está configurado como um host de função (isFunctionHost = false).");
        }
        
        function.setDevice(targetDevice);

        try {
            return functionRepository.save(function);
        } catch (Exception e) {
            throw new ResourceCreationException("Erro ao criar a instância da função: " + e.getMessage());
        }
    }

    @GetMapping("/find-all")
    public List<Function> findAll(){
        return functionRepository.findAll();
    }

    @PutMapping("/{id}/edit-function-name")
    public ResponseEntity<Function> updateFunctionName(@PathVariable Long id, @RequestParam String newName){
        if (newName == null || newName.trim().isEmpty()){
            throw new AttributeException("O novo nome deve ser informado");
        }

         return functionRepository.findById(id)
            .map(function -> {
                function.setName(newName);
                Function updated = functionRepository.save(function);
                return ResponseEntity.ok(updated);
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar a função com o id: " + id));
        }

    @PutMapping("/{id}/edit-function-url")
    public ResponseEntity<Function> updateFunctionUrl(@PathVariable Long id, @RequestParam String newUrl){
        if (newUrl == null || newUrl.trim().isEmpty()){
            throw new AttributeException("O novo URL deve ser informado");
        }

         return functionRepository.findById(id)
            .map(function -> {
                function.setFunctionUrl(newUrl);
                Function updated = functionRepository.save(function);
                return ResponseEntity.ok(updated);
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar a função com o id: " + id));
        }


    @PutMapping("/{id}/edit-device")
    public ResponseEntity<Function> updateFunctionDevice(@PathVariable Long id, @RequestParam Long newDeviceId){
        
        if (newDeviceId == null) 
            throw new AttributeException("O ID do novo dispositivo deve ser informado.");
        


        Device newDevice = deviceRepository.findById(newDeviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado com o id: " + newDeviceId));


        return functionRepository.findById(id)
            .map(function -> {
                function.setDevice(newDevice); 
                Function updated = functionRepository.save(function);
                return ResponseEntity.ok(updated);
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar: Função não encontrada com o id: " + id));
    }
    

    @PutMapping("/{id}")
    public ResponseEntity<Function> updateFunction(@PathVariable Long id, @RequestBody Function functionDetails) {
        if (id == null) {
            throw new AttributeException("O Id da função deve ser informado.");
        }

        return functionRepository.findById(id)
            .map(existingFunction -> {
                
        
                if (functionDetails.getName() != null) {
                    existingFunction.setName(functionDetails.getName());
                }
                if (functionDetails.getDockerImage() != null) {
                    existingFunction.setDockerImage(functionDetails.getDockerImage());
                }
                if (functionDetails.getFunctionUrl() != null) {
                    existingFunction.setFunctionUrl(functionDetails.getFunctionUrl());
                }
                if (functionDetails.getRuntime() != null) {
                    existingFunction.setRuntime(functionDetails.getRuntime());
                }


                if (functionDetails.getDevice() != null && functionDetails.getDevice().getId() != null) {
                    Long newDeviceId = functionDetails.getDevice().getId();
                    

                    if (!existingFunction.getDevice().getId().equals(newDeviceId)) {
                        
                        Device targetDevice = deviceRepository.findById(newDeviceId)
                            .orElseThrow(() -> new ResourceNotFoundException("Dispositivo alvo não encontrado no cluster."));

  
                        if (!targetDevice.getIsFunctionHost()) {
                            throw new AttributeException("Operação negada: O dispositivo selecionado (ID: " 
                                + targetDevice.getId() + ") não suporta execução de funções (isFunctionHost = false).");
                        }
                        
                        existingFunction.setDevice(targetDevice);
                        
                        // TODO: No futuro, a chamada para o FunctionService fazer o Tear Down no nó antigo 
                        // e o Spin Up no nó novo entrará aqui, antes de salvar no banco.
                    }
                }

                try {
                    Function updated = functionRepository.save(existingFunction);
                    return ResponseEntity.ok(updated);
                } catch (Exception e) {
                    throw new ResourceEditException("Erro ao editar a função: " + e.getMessage());
                }
                
            }).orElseThrow(() -> new ResourceNotFoundException("Função não encontrada com o id: " + id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id) {
        if (functionRepository.existsById(id)) {
            functionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<Function> findById(@PathVariable Long id){
        return functionRepository.findById(id)
        .map(function -> ResponseEntity.ok(function))
        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/find-by-name")
    public List<Function> findByName(@RequestParam String name){
        return functionRepository.findByName(name);
    }

    @GetMapping("/find-by-device-name")
    public List<Function> findByDeviceName(@RequestParam String name){
        return functionRepository.findByDeviceName(name);
    }

    @GetMapping("/{id}/created-date")
    public ResponseEntity<Function> getCreationDate(@PathVariable Long id) {
        if (id == null) {
            throw new AttributeException("O Id da funcao deve ser informado");
        }
        
        return functionRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("funcao não encontrada"));
    }    

    


    
    
}
