package com.portfolio.faasmanager.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.*;

@Entity
@Table(name = "tb_execution_log")
@EntityListeners(AuditingEntityListener.class)
public class ExecutionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate 
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt; 

    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt;

    @Column(name = "status_code", updatable = false)
    private Integer statusCode;

    @Column(name = "request_duration_ms", updatable = false)
    private Long requestDurationMs;

    @Column(nullable = false, updatable = false)
    private Boolean success;

    @ManyToOne
    @JoinColumn(name = "function_instance_id", nullable = false, updatable = false)
    private FunctionInstance functionInstance;

    public ExecutionLog() {}

    public ExecutionLog(Long requestDurationMs, boolean success, FunctionInstance functionInstance) {
        this.requestDurationMs = requestDurationMs;
        this.success = success;
        this.functionInstance = functionInstance;
    }


    public Long getId() { return id; }
    public LocalDateTime getSavedAt() { return savedAt; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public Integer getStatusCode() { return statusCode; }
    public Long getRequestDurationMs() { return requestDurationMs; }
    public boolean isSuccess() { return success; }
    public FunctionInstance getFunctionInstance() { return functionInstance; }


    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
}