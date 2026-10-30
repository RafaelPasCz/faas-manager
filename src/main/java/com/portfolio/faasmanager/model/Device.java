/*Arquivo para gerar a tabela Device para o banco de dados */

package com.portfolio.faasmanager.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_device")
@EntityListeners(AuditingEntityListener.class)

public class Device{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Chave primária incremental
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    //deixar o prometheus opcional pois pode ter dispositivos que não suportam
    @Column(name = "prometheus_url", length = 300, nullable = true, unique = true)
    private String prometheusUrl;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Function> functions;

    @CreatedDate 
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    public Device(){

    }

    public Device(String name, String prometheusUrl){
        this.name = name;
        this.prometheusUrl = prometheusUrl;


    }

    public String getName(){
        return name;
    }

    public Long getId(){
        return id;
    }

    public String getPrometheusUrl(){
        return prometheusUrl;
    }

    public LocalDateTime getSavedAt(){
        return savedAt;
    }

    public void setName(String newName){
        this.name = newName;
    }

    public void setPrometheusUrl(String newPrometheusUrl){
        this.prometheusUrl = newPrometheusUrl;
    }

    


}