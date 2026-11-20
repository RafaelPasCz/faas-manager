package com.portfolio.faasmanager.repository;

import java.util.Optional;
import java.util.List;

import com.portfolio.faasmanager.model.Function;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FunctionRepository extends JpaRepository<Function, Long> {

  
    Optional<Function> findByName(String name);

    Optional<Function> findByNameAndActiveTrue(String name);

    boolean existsByNameAndActiveTrue(String name);

    boolean existsByNameAndActiveTrueAndIdNot(String name, Long id);

    List<Function> findByRuntime(String runtime);
    
    List<Function> findAllByActiveTrue();
    
    Optional<Function> findByIdAndActiveTrue(Long id);
    
}