package com.ciphercore.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an immutable block on the BharatVidya / AICTE
 * Consortium Proof-of-Authority (PoA) Blockchain (Chain ID: 26261).
 */
@Entity
@Table(name = "blockchain_blocks")
public class BlockchainBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "block_number", unique = true, nullable = false)
    private Long blockNumber;

    @Column(name = "previous_block_hash", nullable = false, length = 66)
    private String previousBlockHash;

    @Column(name = "block_hash", nullable = false, length = 66)
    private String blockHash;

    @Column(name = "merkle_root", nullable = false, length = 66)
    private String merkleRoot;

    @Column(name = "validator_address", nullable = false, length = 42)
    private String validatorAddress;

    @Column(name = "validator_name", nullable = false, length = 128)
    private String validatorName;

    @Column(name = "validator_signature", length = 132)
    private String validatorSignature;

    @Column(name = "gas_used", nullable = false)
    private Long gasUsed;

    @Column(name = "gas_limit", nullable = false)
    private Long gasLimit;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime timestamp;

    @Lob
    @Column(name = "transactions_payload", columnDefinition = "CLOB")
    private String transactionsPayload;

    @Column(name = "is_tampered", nullable = false)
    private Boolean isTampered = false;

    public BlockchainBlock() {
    }

    public BlockchainBlock(Long blockNumber, String previousBlockHash, String blockHash,
                           String merkleRoot, String validatorAddress, String validatorName,
                           String validatorSignature, Long gasUsed, Long gasLimit,
                           Integer transactionCount, LocalDateTime timestamp,
                           String transactionsPayload) {
        this.blockNumber = blockNumber;
        this.previousBlockHash = previousBlockHash;
        this.blockHash = blockHash;
        this.merkleRoot = merkleRoot;
        this.validatorAddress = validatorAddress;
        this.validatorName = validatorName;
        this.validatorSignature = validatorSignature;
        this.gasUsed = gasUsed;
        this.gasLimit = gasLimit;
        this.transactionCount = transactionCount;
        this.timestamp = timestamp;
        this.transactionsPayload = transactionsPayload;
        this.isTampered = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }

    public String getPreviousBlockHash() { return previousBlockHash; }
    public void setPreviousBlockHash(String previousBlockHash) { this.previousBlockHash = previousBlockHash; }

    public String getBlockHash() { return blockHash; }
    public void setBlockHash(String blockHash) { this.blockHash = blockHash; }

    public String getMerkleRoot() { return merkleRoot; }
    public void setMerkleRoot(String merkleRoot) { this.merkleRoot = merkleRoot; }

    public String getValidatorAddress() { return validatorAddress; }
    public void setValidatorAddress(String validatorAddress) { this.validatorAddress = validatorAddress; }

    public String getValidatorName() { return validatorName; }
    public void setValidatorName(String validatorName) { this.validatorName = validatorName; }

    public String getValidatorSignature() { return validatorSignature; }
    public void setValidatorSignature(String validatorSignature) { this.validatorSignature = validatorSignature; }

    public Long getGasUsed() { return gasUsed; }
    public void setGasUsed(Long gasUsed) { this.gasUsed = gasUsed; }

    public Long getGasLimit() { return gasLimit; }
    public void setGasLimit(Long gasLimit) { this.gasLimit = gasLimit; }

    public Integer getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getTransactionsPayload() { return transactionsPayload; }
    public void setTransactionsPayload(String transactionsPayload) { this.transactionsPayload = transactionsPayload; }

    public Boolean getIsTampered() { return isTampered; }
    public void setIsTampered(Boolean isTampered) { this.isTampered = isTampered; }
}
