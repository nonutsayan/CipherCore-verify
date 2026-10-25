package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.ApplicationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationCategoryRepository extends JpaRepository<ApplicationCategory, Long> {
    Optional<ApplicationCategory> findByCode(String code);
    List<ApplicationCategory> findByActiveTrue();
}
