package com.udf.microservice.repository;

import com.udf.microservice.domain.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {

    List<ReportTemplate> findByEntityType(String entityType);

    ReportTemplate findByName(String name);
}
