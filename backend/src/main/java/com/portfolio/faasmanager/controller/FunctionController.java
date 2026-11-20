package com.portfolio.faasmanager.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.portfolio.faasmanager.exception.*;
import com.portfolio.faasmanager.model.Function;
import com.portfolio.faasmanager.repository.FunctionRepository;

@RestController
@RequestMapping("/api/function")
public class FunctionController {
    
    private final FunctionRepository functionRepository;

    public FunctionController(FunctionRepository functionRepository) {
        this.functionRepository = functionRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Function createFunction(@RequestBody Function function) {
        if (function.getName() == null || function.getName().trim().isEmpty()) {
            throw new AttributeException("O nome da função deve ser informado.");
        }

        String trimmedName = function.getName().trim();
        function.setName(trimmedName);

        if (functionRepository.existsByNameAndActiveTrue(trimmedName)) {
            throw new ResourceConflictException("Já existe uma função ativa com o nome '" + trimmedName + "'.");
        }

        try {
            return functionRepository.save(function);
        } catch (Exception e) {
            throw new ResourceCreationException("Erro ao criar a função: " + e.getMessage());
        }
    }

    @GetMapping("/find-all")
    public List<Function> findAll() {
        return functionRepository.findAll();
    }

    @PutMapping("/{id}/edit-function-name")
    public ResponseEntity<Function> updateFunctionName(@PathVariable Long id, @RequestParam String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new AttributeException("O novo nome deve ser informado");
        }
        String trimmedName = newName.trim();
        if (functionRepository.existsByNameAndActiveTrueAndIdNot(trimmedName, id)) {
            throw new ResourceConflictException("Já existe outra função ativa com o nome '" + trimmedName + "'.");
        }
        return functionRepository.findById(id)
            .map(function -> {
                function.setName(trimmedName);
                return ResponseEntity.ok(functionRepository.save(function));
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar a função com o id: " + id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Function> updateFunction(@PathVariable Long id, @RequestBody Function functionDetails) {
        if (id == null) {
            throw new AttributeException("O Id da função deve ser informado.");
        }
        if (functionDetails.getName() != null) {
            String trimmedName = functionDetails.getName().trim();
            if (trimmedName.isEmpty()) {
                throw new AttributeException("O nome da função não pode ser vazio.");
            }
            if (functionRepository.existsByNameAndActiveTrueAndIdNot(trimmedName, id)) {
                throw new ResourceConflictException("Já existe outra função ativa com o nome '" + trimmedName + "'.");
            }
        }
        return functionRepository.findById(id)
            .map(existingFunction -> {
                if (functionDetails.getName() != null) existingFunction.setName(functionDetails.getName().trim());
                if (functionDetails.getDockerImage() != null) existingFunction.setDockerImage(functionDetails.getDockerImage());
                if (functionDetails.getRuntime() != null) existingFunction.setRuntime(functionDetails.getRuntime());
                
                try {
                    return ResponseEntity.ok(functionRepository.save(existingFunction));
                } catch (Exception e) {
                    throw new ResourceEditException("Erro ao editar a função: " + e.getMessage());
                }
            }).orElseThrow(() -> new ResourceNotFoundException("Função não encontrada com o id: " + id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFunction(@PathVariable Long id) {
        if (id == null) throw new AttributeException("O id não deve ser nulo");
        
        return functionRepository.findById(id)
            .map(function -> {
              
                function.setActive(false);
                
                functionRepository.save(function); 
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseThrow(() -> new ResourceDeleteException("Erro ao deletar a função: " + id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Function> findById(@PathVariable Long id) {
        return functionRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/find-by-name")
    public ResponseEntity<Function> findByName(@RequestParam String name) {
        return functionRepository.findByNameAndActiveTrue(name)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}