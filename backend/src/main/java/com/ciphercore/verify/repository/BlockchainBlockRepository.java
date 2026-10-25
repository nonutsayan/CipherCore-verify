package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.BlockchainBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockchainBlockRepository extends JpaRepository<BlockchainBlock, Long> {
    Optional<BlockchainBlock> findByBlockNumber(Long blockNumber);
    Optional<BlockchainBlock> findByBlockHash(String blockHash);
    Optional<BlockchainBlock> findTopByOrderByBlockNumberDesc();
    List<BlockchainBlock> findAllByOrderByBlockNumberDesc();
    List<BlockchainBlock> findAllByOrderByBlockNumberAsc();
}
