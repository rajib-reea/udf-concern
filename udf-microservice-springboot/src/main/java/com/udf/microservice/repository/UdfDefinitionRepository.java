package com.udf.microservice.repository;

import com.udf.microservice.domain.UdfDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UdfDefinitionRepository extends JpaRepository<UdfDefinition, Long> {

    List<UdfDefinition> findByEntityType(String entityType);

    Optional<UdfDefinition> findByEntityTypeAndFieldName(String entityType, String fieldName);

    boolean existsByEntityTypeAndFieldName(String entityType, String fieldName);

    @Query("SELECT ud FROM UdfDefinition ud WHERE ud.entityType = :entityType AND ud.fieldName != :excludeFieldName")
    List<UdfDefinition> findByEntityTypeExcludingField(@Param("entityType") String entityType,
                                                      @Param("excludeFieldName") String excludeFieldName);
}