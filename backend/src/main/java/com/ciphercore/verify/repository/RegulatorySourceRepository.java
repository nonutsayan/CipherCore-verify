package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.RegulatorySource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RegulatorySourceRepository extends JpaRepository<RegulatorySource, Long> {
    Optional<RegulatorySource> findBySourceCode(String sourceCode);
}
