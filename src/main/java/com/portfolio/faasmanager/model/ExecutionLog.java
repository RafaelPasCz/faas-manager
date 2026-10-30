/* Arquivo para guardar os dados de cada execução no banco de dados */

package com.portfolio.faasmanager.model;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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


    @Column(name = "executed_at", nullable = false, updatable = false) // updatable = false impede que alguém altere a data do log depois
    private LocalDateTime executedAt;

    @Column(name = "status_code", updatable = false)
    private Integer statusCode;

    @Column(name = "request_duration_ms", updatable = false)
    private Long requestDurationMs;

    @Column(nullable = false, updatable = false)
    private Boolean success;

    //chave estrangeira para function
    //uma função pode ter várias logs de execução
    //cada log tem apenas uma função
    //o objeto function ja tem um device_id como FK, então ja temos essa info
    @ManyToOne
    @JoinColumn(name = "function_id", nullable = false, updatable = false)
    private Function function;

    public ExecutionLog(){

    }

    public ExecutionLog(Long requestDurationMs, boolean success, Function function){
        this.requestDurationMs = requestDurationMs;
        this.success = success;
        this.function = function;
    }



    public Long getRequestDurationMs(){
        return requestDurationMs;
    }

    public boolean isSuccess(){
        return success;
    }

    public Function getFunction(){
        return function;
    }

    public LocalDateTime getExecutedAt(){
        return executedAt;
    }

    public Long getId(){
        return id;
    }

    public LocalDateTime getSavedAt(){
        return savedAt;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

}