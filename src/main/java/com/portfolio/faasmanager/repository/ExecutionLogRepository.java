package com.portfolio.faasmanager.repository;

import com.portfolio.faasmanager.model.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository    

public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {

    List<ExecutionLog> findBySuccess(Boolean success);

    List<ExecutionLog> findByFunction_Name(String functionName);

    List<ExecutionLog> findByFunction_Device_Name(String deviceName);

    List<ExecutionLog> findByExecutedAtBetween(LocalDateTime start, LocalDateTime end);    
    
    
}
