package com.portfolio.faasmanager.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.*;

@Entity
@Table(name = "tb_function_instance")
@EntityListeners(AuditingEntityListener.class)
public class FunctionInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean active = true; 

    @ManyToOne(optional = false)
    @JoinColumn(name = "function_id", nullable = false)
    private Function function;

    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false, length = 20)
    private String status = "DEPLOYING"; 

    @CreatedDate
    @Column(name = "deployed_at", nullable = false, updatable = false)
    private LocalDateTime deployedAt;

    public FunctionInstance() {}

    public Long getId() { return id; }
    public Function getFunction() { return function; }
    public Device getDevice() { return device; }
    public String getStatus() { return status; }
    public LocalDateTime getDeployedAt() { return deployedAt; }
    public Boolean getActive() { return active; }

    public void setFunction(Function function) { this.function = function; }
    public void setDevice(Device device) { this.device = device; }
    public void setStatus(String status) { this.status = status; }
    public void setActive(Boolean active) { this.active = active; }
}