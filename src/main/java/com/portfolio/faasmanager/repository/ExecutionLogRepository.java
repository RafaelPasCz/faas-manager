package com.portfolio.faasmanager.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.portfolio.faasmanager.model.ExecutionLog;

@Repository    

public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {

    List<ExecutionLog> findBySuccess(Boolean success);

    List<ExecutionLog> findByFunction_Name(String functionName);

    List<ExecutionLog> findByFunction_Device_Name(String deviceName);

    List<ExecutionLog> findByExecutedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<ExecutionLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);    

    List<ExecutionLog> findByStatusCode(Integer statusCode);
    
    
}
