package com.udf.microservice.repository;

import com.udf.microservice.domain.EntityUdfValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface EntityUdfValueRepository extends JpaRepository<EntityUdfValue, Long> {

    List<EntityUdfValue> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<EntityUdfValue> findByEntityTypeAndFieldId(String entityType, Long fieldId);

    EntityUdfValue findByEntityTypeAndEntityIdAndFieldId(String entityType, Long entityId, Long fieldId);

    @Query("SELECT euv FROM EntityUdfValue euv WHERE euv.entityType = :entityType AND euv.entityId IN :entityIds")
    List<EntityUdfValue> findByEntityTypeAndEntityIds(@Param("entityType") String entityType,
                                                     @Param("entityIds") List<Long> entityIds);

    @Query("SELECT new map(euv.entityId as entityId, euv.fieldId as fieldId, euv.value as value) " +
           "FROM EntityUdfValue euv WHERE euv.entityType = :entityType AND euv.entityId = :entityId")
    List<Map<String, Object>> findValuesAsMap(@Param("entityType") String entityType,
                                             @Param("entityId") Long entityId);
}