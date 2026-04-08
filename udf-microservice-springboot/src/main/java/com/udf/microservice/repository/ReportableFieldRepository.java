package com.udf.microservice.repository;

import com.udf.microservice.domain.ReportableField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportableFieldRepository extends JpaRepository<ReportableField, Long> {

    List<ReportableField> findByEntity(String entity);

    Optional<ReportableField> findByEntityAndFieldName(String entity, String fieldName);

    List<ReportableField> findByEntityAndAggregatable(String entity, boolean aggregatable);

    List<ReportableField> findByEntityAndFilterable(String entity, boolean filterable);
}