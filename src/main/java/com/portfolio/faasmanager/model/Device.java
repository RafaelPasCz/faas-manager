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

    @Column(name = "ip_address", length = 45) // Suporta IPv4
    private String ipAddress;


    @Column(name = "is_prometheus_host", nullable = false)
    private boolean isPrometheusHost = false;

    //Campo exclusivo para devices que são hosts de prometheus
    //TODO: adicionar validação no controller
    @Column(name = "prometheus_url", length = 300, unique = true)
    private String prometheusUrl;

    //Campo exclusivo para devices que NÃO são hosts de prometheus
    //TODO: adicionar validação no controller
    @Column(name = "prometheus_host_id")
    private Long prometheusHostId;

    @CreatedDate 
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;


    @Column(name = "is_function_host", nullable = false)
    private boolean isFunctionHost = false;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Function> functions;

    @Column(name = "node_exporter_url", length = 300)
    private String nodeExporterUrl;

    public Device(){

    }

    public Device(String name, String prometheusUrl){
        this.name = name;
        this.prometheusUrl = prometheusUrl;


    }

    public Long getPrometheusHostId(){
        return prometheusHostId;
    }


    public String getNodeExporterUrl() {
        return nodeExporterUrl;
    }

    public String getName(){
        return name;
    }

    public String getIpAddress(){
        return ipAddress;
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

    public boolean getIsPrometheusHost(){
        return isPrometheusHost;
    }

    public boolean getIsFunctionHost(){
        return isFunctionHost;
    }

    public List<Function> getFunctions(){
        return functions;
    }


    public void setPrometheusHostId(Long newPrometheusHostId){
        this.prometheusHostId = newPrometheusHostId;
    }

    public void setIsPrometheusHost(boolean newIsPrometheusHost){
        this.isPrometheusHost = newIsPrometheusHost;
    }

    public void setIsFunctionHost(boolean newIsFunctionHost){
        this.isFunctionHost = newIsFunctionHost;
    }


    public void setNodeExporterUrl(String nodeExporterUrl) {
        this.nodeExporterUrl = nodeExporterUrl;
    }



    public void setName(String newName){
        this.name = newName;
    }

    public void setPrometheusUrl(String newPrometheusUrl){
        this.prometheusUrl = newPrometheusUrl;
    }

    public void setIpAddress(String newIpAdress){
        this.ipAddress = newIpAdress;
    }

    


}