package com.portfolio.faasmanager.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.portfolio.faasmanager.exception.*;
import com.portfolio.faasmanager.model.ExecutionLog;
import com.portfolio.faasmanager.repository.ExecutionLogRepository;

@RestController
@RequestMapping("/api/execution-logs")
@CrossOrigin(origins = "http://localhost:4200")
public class ExecutionLogController {
    
    private final ExecutionLogRepository repository;

    public ExecutionLogController(ExecutionLogRepository repository) {
        this.repository = repository;
    }    

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExecutionLog createNewExecutionLog(@RequestBody ExecutionLog log) {
        if(log.getExecutedAt() == null) {
            throw new AttributeException("Log não salva: Data e hora da execução faltando");
        }
        if (log.getFunctionInstance() == null) {
            throw new AttributeException("Log não salva: Instância da função faltando");
        }
        return repository.save(log);
    }

    @GetMapping("/find-all")
    public List<ExecutionLog> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionLog> findById(@PathVariable Long id) {
        return repository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/find-by-function-name")
    public List<ExecutionLog> findByFunctionName(@RequestParam String name) {
        return repository.findByFunctionInstance_Function_Name(name);
    }

    @GetMapping("/find-by-device-name")
    public List<ExecutionLog> findByFunctionDeviceName(@RequestParam String name) {
        return repository.findByFunctionInstance_Device_Name(name);
    }
    
    @GetMapping("/find-by-executed-between")
    public List<ExecutionLog> findByExecutedAtBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start, 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return repository.findByExecutedAtBetween(start, end);
    }

    @GetMapping("/find-by-saved-between")
    public List<ExecutionLog> findBySavedAtBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start, 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return repository.findBySavedAtBetween(start, end);
    }

    @GetMapping("/device/{deviceId}")
    public List<ExecutionLog> findByDeviceId(@PathVariable Long deviceId) {
        return repository.findByFunctionInstance_DeviceIdOrderByExecutedAtDesc(deviceId);
    }
}