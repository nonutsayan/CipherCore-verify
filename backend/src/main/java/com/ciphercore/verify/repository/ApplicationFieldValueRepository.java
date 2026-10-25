package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.ApplicationFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationFieldValueRepository extends JpaRepository<ApplicationFieldValue, Long> {
    List<ApplicationFieldValue> findByApplicationId(Long applicationId);
    Optional<ApplicationFieldValue> findByApplicationIdAndFieldName(Long applicationId, String fieldName);
}
