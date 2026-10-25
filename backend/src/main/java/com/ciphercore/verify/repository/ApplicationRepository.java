package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.Application;
import com.ciphercore.verify.model.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByApplicationRef(String applicationRef);

    List<Application> findByCreatedBy(String createdBy);

    List<Application> findByCategoryCode(String categoryCode);

    List<Application> findByAcademicYear(String academicYear);

    long countByStatus(ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           " LOWER(a.applicationRef) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(a.institutionName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(a.instituteCode) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:categoryCode IS NULL OR :categoryCode = '' OR a.categoryCode = :categoryCode) AND " +
           "(:academicYear IS NULL OR :academicYear = '' OR a.academicYear = :academicYear) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Application> searchApplications(
            @Param("query") String query,
            @Param("categoryCode") String categoryCode,
            @Param("academicYear") String academicYear,
            @Param("status") ApplicationStatus status,
            Pageable pageable
    );
}
