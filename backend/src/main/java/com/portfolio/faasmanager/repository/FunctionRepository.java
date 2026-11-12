package com.portfolio.faasmanager.repository;

import java.util.Optional;
import java.util.List;

import com.portfolio.faasmanager.model.Device;
import com.portfolio.faasmanager.model.Function;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FunctionRepository extends JpaRepository<Function, Long> {

  
    Optional<Function> findByName(String name);

    List<Function> findByRuntime(String runtime);
    
    List<Device> findAllByActiveTrue();
    
    Optional<Device> findByIdAndActiveTrue(Long id);
    
}