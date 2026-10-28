package com.portfolio.faasmanager.repository;

import java.util.Optional;
import java.util.List;
import com.portfolio.faasmanager.model.Function;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FunctionRepository extends JpaRepository<Function, Long>{

    List<Function> findByName(String name);

    List<Function> findByImplementationModel(String implementationModel);

    Optional<Function> findByFunctionUrl(String functionUrl);

    List<Function> findByDeviceId(Long deviceId);

    List<Function> findByDeviceName(String deviceName);
    
}
