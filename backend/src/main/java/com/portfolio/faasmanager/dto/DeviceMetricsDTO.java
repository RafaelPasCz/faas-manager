package com.portfolio.faasmanager.dto;

public class DeviceMetricsDTO {
    private Double cpu;
    private Double ram;

    public DeviceMetricsDTO(Double cpu, Double ram) {
        this.cpu = cpu;
        this.ram = ram;
    }

    public Double getCpu() { return cpu; }
    public Double getRam() { return ram; }
    
    public void setCpu(Double cpu) { this.cpu = cpu; }
    public void setRam(Double ram) { this.ram = ram; }
}