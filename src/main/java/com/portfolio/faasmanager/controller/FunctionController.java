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
@CrossOrigin(origins = "http://localhost:4200")
public class FunctionController {
    
    private final FunctionRepository functionRepository;

    public FunctionController(FunctionRepository functionRepository) {
        this.functionRepository = functionRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Function createFunction(@RequestBody Function function) {
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
        return functionRepository.findById(id)
            .map(function -> {
                function.setName(newName);
                return ResponseEntity.ok(functionRepository.save(function));
            }).orElseThrow(() -> new ResourceEditException("Erro ao editar a função com o id: " + id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Function> updateFunction(@PathVariable Long id, @RequestBody Function functionDetails) {
        if (id == null) {
            throw new AttributeException("O Id da função deve ser informado.");
        }
        return functionRepository.findById(id)
            .map(existingFunction -> {
                if (functionDetails.getName() != null) existingFunction.setName(functionDetails.getName());
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
        if (functionRepository.existsById(id)) {
            functionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Function> findById(@PathVariable Long id) {
        return functionRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/find-by-name")
    public ResponseEntity<Function> findByName(@RequestParam String name) {
        return functionRepository.findByName(name)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}