package com.portfolio.faasmanager.controller;

import com.portfolio.faasmanager.model.Device;
import com.portfolio.faasmanager.model.Function;
import com.portfolio.faasmanager.repository.DeviceRepository;
import com.portfolio.faasmanager.repository.FunctionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.portfolio.faasmanager.exception.ResourceNotFoundException;
import com.portfolio.faasmanager.exception.ResourceEditException;
import com.portfolio.faasmanager.exception.ResourceDeleteException;
import com.portfolio.faasmanager.exception.ResourceCreationException;
import com.portfolio.faasmanager.exception.AttributeException;
import com.portfolio.faasmanager.exception.GeneralException;

import java.util.List;


@RestController
@RequestMapping("/functions")
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
    public Function createNewFunction(@RequestBody Function function){
        return functionRepository.save(function);
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

    @PutMapping("{id}/edit-implementation-model")
    public ResponseEntity<Function> updateFunctionImplementationModel(@PathVariable Long id, @RequestParam String newModel){
        if(newModel == null || newModel.trim().isEmpty()){
            throw new AttributeException("O novo modelo de implementação deve ser informado");
        }

        return functionRepository.findById(id)
        .map(function -> {
            function.setImplementationModel(newModel);
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
        if (functionDetails.getName() == null) 
            throw new AttributeException("O nome não deve ser nulo");
        
        if (functionDetails.getFunctionUrl() == null) 
            throw new AttributeException("A url da função não deve ser nula");
        
        if (functionDetails.getDevice() == null) 
            throw new AttributeException("O Id do novo dispositivo deve ser informado");
        
        
        return functionRepository.findById(id)
            .map(function -> {
                function.setName(functionDetails.getName());
                function.setFunctionUrl(functionDetails.getFunctionUrl());
                function.setImplementationModel(functionDetails.getImplementationModel());
                function.setDevice(functionDetails.getDevice());
                Function updated = functionRepository.save(function);
                return ResponseEntity.ok(updated);
            })
            .orElseThrow(() -> new ResourceEditException("Erro ao editar o dispostivo" + id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id) {
        if (functionRepository.existsById(id)) {
            functionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/delete-implementation-model")
    public ResponseEntity<Void> deleteImplementationModel(@PathVariable Long id){
        return functionRepository.findById(id)
            .map(function -> {
                function.setImplementationModel(null);
                functionRepository.save(function);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.noContent().build()); 
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

    @GetMapping("/find-by-implementation-model")
    public List<Function> findByImplementationModel(@RequestParam String implementationModel){
        return functionRepository.findByImplementationModel(implementationModel);
    }

    


    
    
}
