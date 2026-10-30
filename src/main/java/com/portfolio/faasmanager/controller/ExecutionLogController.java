package com.portfolio.faasmanager.controller;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.faasmanager.exception.AttributeException;
import com.portfolio.faasmanager.exception.ResourceNotFoundException;
import com.portfolio.faasmanager.model.ExecutionLog;
import com.portfolio.faasmanager.repository.ExecutionLogRepository;

@RestController
@RequestMapping("/api/execution-logs")
@CrossOrigin(origins = "http://localhost:4200")

public class ExecutionLogController {
    private final ExecutionLogRepository repository;

    public ExecutionLogController(ExecutionLogRepository repository){
        this.repository = repository;


    }    

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExecutionLog createNewExecutionLog(@RequestBody ExecutionLog log){
        if(log.getExecutedAt() == null){
            throw new AttributeException("Log não salva: Data e hora da execução faltando");
        }
        if (log.getFunction() == null){
            throw new AttributeException("Log não salva: Função faltando");
        }
        return repository.save(log);
    }

    @GetMapping("/find-all")
    public List<ExecutionLog> findAll(){
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionLog> findById(@PathVariable Long id){
        return repository.findById(id)
        .map(log -> ResponseEntity.ok(log))
        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/find-by-function-name")
    public List<ExecutionLog> findByFunctionName(@RequestParam String name){
        return repository.findByFunction_Name(name);
    }

    @GetMapping("/find-by-device-name")
    public List<ExecutionLog> findByFunctionDeviceName(@RequestParam String name){
        return repository.findByFunction_Device_Name(name);
    }
    
    @GetMapping("/find-by-executed-between")
    public List<ExecutionLog> findByExecutedAtBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start, 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end){
        return repository.findByExecutedAtBetween(start, end);
    }

    @GetMapping("/find-by-created-between")
    public List<ExecutionLog> findByCreatedAtBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start, 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end){
        return repository.findByCreatedAtBetween(start, end);
    }


    @GetMapping("/{id}/created-date")
    public ResponseEntity<ExecutionLog> getCreationDate(@PathVariable Long id) {
        if (id == null) {
            throw new AttributeException("O Id da log deve ser informado");
        }
        
        return repository.findById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("log não encontrada"));
    }

    @GetMapping("/{id}/status-code")
    public ResponseEntity<ExecutionLog> getStatusCode(@PathVariable Long id) {
        if (id == null) {
            throw new AttributeException("O Id do log deve ser informado");
        }
        
        return repository.findById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("Log de execução não encontrado"));
    }

    @GetMapping("/find-by-status")
    public List<ExecutionLog> findByStatusCode(@RequestParam Integer statusCode) {
        if (statusCode == null) {
            throw new AttributeException("O status code deve ser informado para a busca");
        }
        
        return repository.findByStatusCode(statusCode);
    }

}
