package com.portfolio.faasmanager.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.portfolio.faasmanager.model.FunctionInstance;

@Repository
public interface FunctionInstanceRepository extends JpaRepository<FunctionInstance, Long> {
    

    List<FunctionInstance> findByStatus(String status);
    

    List<FunctionInstance> findByFunction_Name(String functionName);
    
    List<FunctionInstance> findByDevice_Name(String deviceName);
    
    Optional<FunctionInstance> findByFunctionIdAndDeviceId(Long functionId, Long deviceId);
}