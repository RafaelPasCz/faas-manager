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
@Table(name = "tb_function")
@EntityListeners(AuditingEntityListener.class)
public class Function {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "docker_image", nullable = false, length = 200)
    private String dockerImage;

    @Column(name = "function_url", nullable = false, length = 300)
    private String functionUrl;

    @Column(nullable = false, length = 50)
    private String runtime;

    @CreatedDate
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    public Function() {}

    public Long getId(){ 
        return id; 
    }

    public String getName() { 
        return name;
    }

    public String getDockerImage() {
        return dockerImage; 
    }

    public String getFunctionUrl() { 
        return functionUrl; 
    }

    public String getRuntime() { 
        return runtime; 
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public Device getDevice() { 
        return device;
     }


    public void setName(String name) {
        this.name = name;
    }

    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public void setFunctionUrl(String functionUrl) { 
        this.functionUrl = functionUrl; 
    }

    public void setRuntime(String runtime) { 
        this.runtime = runtime; 
    }

    public void setDevice(Device device) {
        this.device = device;
    }

}