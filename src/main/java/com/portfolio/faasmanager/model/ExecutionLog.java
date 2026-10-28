/* Arquivo para guardar os dados de cada execução no banco de dados */

package com.portfolio.faasmanager.model;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import com.portfolio.faasmanager.model.Function;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_execution_log")
public class ExecutionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "executed_at", nullable = false, updatable = false) // updatable = false impede que alguém altere a data do log depois
    private LocalDateTime executedAt;

    @Column(name = "request_duration_ms", nullable = false, updatable = false)
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

    public void setSuccess(boolean newSuccess){
        this.success = newSuccess;
    }

    public void setFunction(Function newFunction){
        this.function = newFunction;
    }

    public void setExecutedAt(LocalDateTime newExecutedAt){
        this.executedAt = newExecutedAt;
    }

    public void setRequestDurationMs(Long newRequestDurationMs){
        this.requestDurationMs = newRequestDurationMs;
    }

}