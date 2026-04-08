package com.udf.microservice.repository;

import com.udf.microservice.domain.FieldType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FieldTypeRepository extends JpaRepository<FieldType, String> {

    Optional<FieldType> findByType(String type);
}