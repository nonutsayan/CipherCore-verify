package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.AuthoritySource;
import com.ciphercore.verify.model.enums.AuthoritySourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthoritySourceRepository extends JpaRepository<AuthoritySource, Long> {
    Optional<AuthoritySource> findBySourceId(String sourceId);
    List<AuthoritySource> findBySourceStatus(AuthoritySourceStatus status);
}
