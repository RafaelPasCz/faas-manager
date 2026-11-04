package com.portfolio.faasmanager.model;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.*;

@Entity
@Table(name = "tb_device")
@EntityListeners(AuditingEntityListener.class)
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "is_prometheus_host", nullable = false)
    private boolean isPrometheusHost = false;

    @Column(name = "prometheus_url", length = 300, unique = true)
    private String prometheusUrl;

    @Column(name = "prometheus_host_id")
    private Long prometheusHostId;

    @Column(name = "gateway_port")
    private Integer gatewayPort;

    @CreatedDate 
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    @Column(name = "is_function_host", nullable = false)
    private boolean isFunctionHost = false;


    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FunctionInstance> functionInstances;

    @Column(name = "node_exporter_url", length = 300)
    private String nodeExporterUrl;

    public Device() {}

    public Device(String name, String prometheusUrl) {
        this.name = name;
        this.prometheusUrl = prometheusUrl;
    }

  
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getIpAddress() { return ipAddress; }
    public boolean getIsPrometheusHost() { return isPrometheusHost; }
    public String getPrometheusUrl() { return prometheusUrl; }
    public Long getPrometheusHostId() { return prometheusHostId; }
    public Integer getGatewayPort() { return gatewayPort; }
    public LocalDateTime getSavedAt() { return savedAt; }
    public boolean getIsFunctionHost() { return isFunctionHost; }
    public List<FunctionInstance> getFunctionInstances() { return functionInstances; }
    public String getNodeExporterUrl() { return nodeExporterUrl; }


    public void setName(String name) { this.name = name; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setIsPrometheusHost(boolean isPrometheusHost) { this.isPrometheusHost = isPrometheusHost; }
    public void setPrometheusUrl(String prometheusUrl) { this.prometheusUrl = prometheusUrl; }
    public void setPrometheusHostId(Long prometheusHostId) { this.prometheusHostId = prometheusHostId; }
    public void setGatewayPort(Integer gatewayPort) { this.gatewayPort = gatewayPort; }
    public void setIsFunctionHost(boolean isFunctionHost) { this.isFunctionHost = isFunctionHost; }
    public void setNodeExporterUrl(String nodeExporterUrl) { this.nodeExporterUrl = nodeExporterUrl; }
}