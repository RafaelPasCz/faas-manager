package com.portfolio.faasmanager.repository;

import com.portfolio.faasmanager.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository                                           
public interface DeviceRepository extends JpaRepository<Device, Long> {
   
    Optional<Device> findByName(String name);

    Optional<Device> findByPrometheusUrl(String prometheusUrl);

    List<Device> findByIsFunctionHostTrue();

    List<Device> findByIsPrometheusHostTrue();
}