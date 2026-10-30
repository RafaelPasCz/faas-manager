package com.portfolio.faasmanager.controller;
import org.springframework.format.annotation.DateTimeFormat;

import com.portfolio.faasmanager.exception.AttributeException;
import com.portfolio.faasmanager.model.ExecutionLog;
import com.portfolio.faasmanager.repository.ExecutionLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.List;

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
    

}
