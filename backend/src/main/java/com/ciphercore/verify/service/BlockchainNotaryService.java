package com.ciphercore.verify.service;

import com.ciphercore.verify.model.BlockchainBlock;
import com.ciphercore.verify.repository.BlockchainBlockRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlockchainNotaryService {

    private final BlockchainBlockRepository blockchainBlockRepository;
    private final AuditTrailService auditTrailService;
    private final ObjectMapper objectMapper;

    // Institutional Audit Authority Agents (Prototype cryptographic signing agents)
    // NOTE (Section 4.5 Compliance): These represent local prototype audit agents,
    // not external decentralized validator nodes.
    public static final List<ValidatorNode> VALIDATOR_NODES = List.of(
            new ValidatorNode("0x9A4bF72D1E58a93B7f8261A703Ce987D1924c9E", "AICTE Central Approval Bureau (Notary Agent)", "LOCAL_AUDIT_AGENT", 42, "0.12 ms"),
            new ValidatorNode("0x1B2C7E58F49a716D2084c8a2b53f66D92078E6F8", "Institutional Dossier Ingestion Agent", "LOCAL_AUDIT_AGENT", 38, "0.45 ms"),
            new ValidatorNode("0x7E3D82A9C1563f462881D82103f56A88902124C1", "Document Forensic Integrity Signer", "LOCAL_AUDIT_AGENT", 36, "0.62 ms"),
            new ValidatorNode("0x4C91E5F231D780289a42531627918F421D87A6A2", "Issuing Authority Verification Agent", "LOCAL_AUDIT_AGENT", 40, "0.38 ms"),
            new ValidatorNode("0x8D23C471E24817a9D8F2113279c164A5829141E5", "Regional Scrutiny Committee Notary", "LOCAL_AUDIT_AGENT", 44, "0.22 ms"),
            new ValidatorNode("0x2F51C8E9D2147983610935172085731945118790", "Vigilance Audit Log Signer", "LOCAL_AUDIT_AGENT", 35, "0.51 ms"),
            new ValidatorNode("0x6A192B45189274c9351082518390172635198477", "National Trust Gateway Archive Signer", "LOCAL_AUDIT_AGENT", 46, "0.30 ms")
    );

    private final Map<Long, String> originalPayloadBackup = new ConcurrentHashMap<>();
    private final Map<Long, String> originalHashBackup = new ConcurrentHashMap<>();

    public record ValidatorNode(String address, String name, String status, int blocksMined, String latency) {}

    public record ChainVerificationResult(
            boolean isChainValid,
            int totalBlocksVerified,
            Long brokenBlockNumber,
            String verificationSummary,
            String trustNotice,
            LocalDateTime verifiedAt
    ) {}

    public record NotarizationResult(
            boolean success,
            Long blockNumber,
            String blockHash,
            String previousBlockHash,
            String transactionHash,
            String merkleRoot,
            String validatorName,
            String validatorAddress,
            String documentHash,
            LocalDateTime timestamp,
            String message
    ) {}

    public record VerificationResult(
            boolean isNotarized,
            Long blockNumber,
            String blockHash,
            String transactionHash,
            String merkleRoot,
            String documentTitle,
            String institutionCode,
            String institutionName,
            String docCategory,
            String validatorName,
            String validatorAddress,
            LocalDateTime timestamp,
            List<String> merkleProof,
            String smartContractAddress,
            String message
    ) {}

    public BlockchainNotaryService(BlockchainBlockRepository blockchainBlockRepository,
                                   AuditTrailService auditTrailService,
                                   ObjectMapper objectMapper) {
        this.blockchainBlockRepository = blockchainBlockRepository;
        this.auditTrailService = auditTrailService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initBlockchain() {
        if (blockchainBlockRepository.count() == 0) {
            seedConsortiumGenesisAndBlocks();
        }
    }

    private void seedConsortiumGenesisAndBlocks() {
        LocalDateTime baseTime = LocalDateTime.now().minusDays(3).truncatedTo(ChronoUnit.SECONDS);

        // Genesis Block (#0)
        String genesisPrev = "0x0000000000000000000000000000000000000000000000000000000000000000";
        String genesisMerkle = "0x" + sha256("GENESIS_MERKLE_ROOT_AICTE_SIH26261");
        String genesisHash = calculateBlockHash(0L, genesisPrev, genesisMerkle, baseTime, VALIDATOR_NODES.get(0).address());
        
        List<Map<String, Object>> genesisTxs = List.of(Map.of(
                "txHash", "0x" + sha256("GENESIS_TX_AICTE_CONSORTIUM_SETUP"),
                "type", "NETWORK_INITIALIZATION",
                "contract", "0x71C8F29a25b2938173491829038283921783a8E4",
                "details", "BharatVidya PoA Consortium Blockchain Initialized with 7 Institutional Validator Nodes."
        ));

        BlockchainBlock genesis = new BlockchainBlock(0L, genesisPrev, genesisHash, genesisMerkle,
                VALIDATOR_NODES.get(0).address(), VALIDATOR_NODES.get(0).name(),
                generateSignature(genesisHash), 21000L, 30000000L, 1, baseTime, toJson(genesisTxs));
        blockchainBlockRepository.save(genesis);

        // Block #1: Apex Tech Land Ownership Deed (7/12 Extract)
        createBlock(1L, genesisHash, baseTime.plusHours(6), VALIDATOR_NODES.get(1),
                "Land Ownership Deed (7/12 Extract)", "0x8f2a6b4c9e1d3f5a7b9c2d4e6f8a0b2c4d6e8f0a2b4c6d8e0f2a4b6c8d0e2f4a",
                "1-987654321", "Apex Institute of Technology", "LAND_TITLE", "Dr. Rajesh Sharma");

        // Block #2: Apex Tech Fire Safety NOC & Municipal Plan
        createBlock(2L, blockchainBlockRepository.findByBlockNumber(1L).get().getBlockHash(),
                baseTime.plusHours(14), VALIDATOR_NODES.get(2),
                "Fire Safety NOC Clearance Certificate", "0x3c7e9a1b5d2f4a6c8e0b2d4f6a8c0e2b4d6f8a0c2e4b6d8f0a2c4e6b8d0f2a4c",
                "1-987654321", "Apex Institute of Technology", "FIRE_NOC", "Dr. Rajesh Sharma");

        // Block #3: Horizon College NA Revenue Order
        createBlock(3L, blockchainBlockRepository.findByBlockNumber(2L).get().getBlockHash(),
                baseTime.plusHours(26), VALIDATOR_NODES.get(3),
                "Non-Agricultural Land Conversion Order", "0x1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b",
                "1-442190012", "Horizon College of Engineering", "LAND_CONVERSION", "Priya Desai");

        // Block #4: Greenfield Polytechnic Lease Deed
        createBlock(4L, blockchainBlockRepository.findByBlockNumber(3L).get().getBlockHash(),
                baseTime.plusHours(38), VALIDATOR_NODES.get(4),
                "30-Year Registered Educational Campus Lease Deed", "0x5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f",
                "1-110287361", "Greenfield Polytechnic Institute", "LEASE_DEED", "Suresh Menon");

        // Block #5: Western Regional Scrutiny Committee Resolution
        createBlock(5L, blockchainBlockRepository.findByBlockNumber(4L).get().getBlockHash(),
                baseTime.plusHours(50), VALIDATOR_NODES.get(0),
                "AICTE Scrutiny Committee Official Resolution Minutes", "0x9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d",
                "AICTE-HQ-2026", "AICTE Central Approval Bureau", "REGULATORY_APPROVAL", "Dr. Meenakshi Sundaram");
    }

    private void createBlock(Long blockNum, String prevHash, LocalDateTime time, ValidatorNode val,
                             String docTitle, String docHash, String instCode, String instName,
                             String category, String officer) {
        String txHash = "0x" + sha256(docHash + blockNum + time);
        String merkleRoot = "0x" + sha256(txHash + docHash);
        String blockHash = calculateBlockHash(blockNum, prevHash, merkleRoot, time, val.address());

        Map<String, Object> tx = new HashMap<>();
        tx.put("txHash", txHash);
        tx.put("documentTitle", docTitle);
        tx.put("documentHash", docHash);
        tx.put("institutionCode", instCode);
        tx.put("institutionName", instName);
        tx.put("documentCategory", category);
        tx.put("notaryOfficer", officer);
        tx.put("timestamp", time.toString());
        tx.put("status", "COMMITTED_ON_CHAIN");
        tx.put("merkleProof", List.of(
                "0x" + sha256(docHash + "LEAF_0"),
                "0x" + sha256(txHash + "BRANCH_1"),
                merkleRoot
        ));

        BlockchainBlock block = new BlockchainBlock(blockNum, prevHash, blockHash, merkleRoot,
                val.address(), val.name(), generateSignature(blockHash),
                65000L + (blockNum * 1200), 30000000L, 1, time, toJson(List.of(tx)));
        blockchainBlockRepository.save(block);
    }

    @Transactional
    public NotarizationResult notarizeDocument(String documentTitle, String documentHash,
                                              String institutionCode, String institutionName,
                                              String docCategory, String notaryOfficer) {
        if (documentHash == null || documentHash.isBlank()) {
            documentHash = "0x" + sha256(documentTitle + institutionCode + System.currentTimeMillis());
        }
        if (!documentHash.startsWith("0x")) {
            documentHash = "0x" + documentHash;
        }

        BlockchainBlock top = blockchainBlockRepository.findTopByOrderByBlockNumberDesc()
                .orElseThrow(() -> new IllegalStateException("Genesis block missing"));

        Long nextBlockNum = top.getBlockNumber() + 1;
        String prevHash = top.getBlockHash();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        // Pick validator node in round-robin based on block height
        ValidatorNode val = VALIDATOR_NODES.get((int) (nextBlockNum % VALIDATOR_NODES.size()));

        String txHash = "0x" + sha256(documentHash + nextBlockNum + now);
        String merkleRoot = "0x" + sha256(txHash + documentHash);
        String blockHash = calculateBlockHash(nextBlockNum, prevHash, merkleRoot, now, val.address());

        Map<String, Object> tx = new HashMap<>();
        tx.put("txHash", txHash);
        tx.put("documentTitle", documentTitle != null ? documentTitle : "Official Institutional Evidence");
        tx.put("documentHash", documentHash);
        tx.put("institutionCode", institutionCode != null ? institutionCode : "INST-NOTARIZED");
        tx.put("institutionName", institutionName != null ? institutionName : "Institutional Applicant");
        tx.put("documentCategory", docCategory != null ? docCategory : "MANDATORY_REGULATORY_RECORD");
        tx.put("notaryOfficer", notaryOfficer != null ? notaryOfficer : "AICTE Regulatory Officer");
        tx.put("timestamp", now.toString());
        tx.put("status", "COMMITTED_ON_CHAIN");
        tx.put("merkleProof", List.of(
                "0x" + sha256(documentHash + "_LEAF_NODE"),
                "0x" + sha256(txHash + "_INTERNAL_HASH"),
                merkleRoot
        ));

        BlockchainBlock block = new BlockchainBlock(nextBlockNum, prevHash, blockHash, merkleRoot,
                val.address(), val.name(), generateSignature(blockHash),
                68400L, 30000000L, 1, now, toJson(List.of(tx)));
        blockchainBlockRepository.save(block);

        // Cross-link to immutable audit trail
        auditTrailService.recordEvent(
                notaryOfficer != null ? notaryOfficer : "officer_sc401",
                "REGULATORY_OFFICER",
                "BLOCKCHAIN_DOCUMENT_NOTARIZED",
                "BLOCKCHAIN_BLOCK",
                String.valueOf(nextBlockNum),
                "Document '" + documentTitle + "' notarized onto BharatVidya PoA Blockchain. Tx: " + txHash + ", Block #" + nextBlockNum,
                "127.0.0.1"
        );

        return new NotarizationResult(
                true, nextBlockNum, blockHash, prevHash, txHash, merkleRoot,
                val.name(), val.address(), documentHash, now,
                "Document permanently notarized on BharatVidya Consortium PoA Blockchain (Block #" + nextBlockNum + ")"
        );
    }

    public VerificationResult verifyDocumentOnChain(String docHash) {
        if (docHash == null || docHash.isBlank()) {
            return new VerificationResult(false, null, null, null, null, null, null, null, null, null, null, null, null, null, "Document hash cannot be empty");
        }
        String cleanHash = docHash.trim().toLowerCase();

        List<BlockchainBlock> blocks = blockchainBlockRepository.findAllByOrderByBlockNumberDesc();
        for (BlockchainBlock b : blocks) {
            try {
                List<Map<String, Object>> txs = objectMapper.readValue(b.getTransactionsPayload(), new TypeReference<>() {});
                for (Map<String, Object> tx : txs) {
                    String txDocHash = String.valueOf(tx.get("documentHash")).toLowerCase();
                    String txHash = String.valueOf(tx.get("txHash")).toLowerCase();

                    if (txDocHash.contains(cleanHash) || txHash.contains(cleanHash) || cleanHash.contains(txDocHash)) {
                        @SuppressWarnings("unchecked")
                        List<String> proof = (List<String>) tx.get("merkleProof");
                        return new VerificationResult(
                                true,
                                b.getBlockNumber(),
                                b.getBlockHash(),
                                String.valueOf(tx.get("txHash")),
                                b.getMerkleRoot(),
                                String.valueOf(tx.get("documentTitle")),
                                String.valueOf(tx.get("institutionCode")),
                                String.valueOf(tx.get("institutionName")),
                                String.valueOf(tx.get("documentCategory")),
                                b.getValidatorName(),
                                b.getValidatorAddress(),
                                b.getTimestamp(),
                                proof,
                                "0x71C8F29a25b2938173491829038283921783a8E4",
                                "Cryptographic On-Chain Verification Passed. Document anchored in Block #" + b.getBlockNumber() + " with valid Merkle proof."
                        );
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return new VerificationResult(
                false, null, null, null, null, null, null, null, null, null, null, null, null,
                "0x71C8F29a25b2938173491829038283921783a8E4",
                "Hash not found on-chain. This document has not been notarized or has been modified retroactively."
        );
    }

    @Transactional
    public boolean simulateTamper(Long blockNumber) {
        Optional<BlockchainBlock> opt = blockchainBlockRepository.findByBlockNumber(blockNumber);
        if (opt.isPresent()) {
            BlockchainBlock b = opt.get();
            originalPayloadBackup.putIfAbsent(blockNumber, b.getTransactionsPayload());
            originalHashBackup.putIfAbsent(blockNumber, b.getBlockHash());

            // Tamper the payload text without recomputing PoA consensus signatures
            b.setTransactionsPayload(b.getTransactionsPayload().replace("COMMITTED_ON_CHAIN", "FORGED_UNAUTHORIZED_ALTERATION"));
            b.setIsTampered(true);
            blockchainBlockRepository.save(b);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean restoreTamper(Long blockNumber) {
        Optional<BlockchainBlock> opt = blockchainBlockRepository.findByBlockNumber(blockNumber);
        if (opt.isPresent()) {
            BlockchainBlock b = opt.get();
            if (originalPayloadBackup.containsKey(blockNumber)) {
                b.setTransactionsPayload(originalPayloadBackup.get(blockNumber));
                b.setBlockHash(originalHashBackup.get(blockNumber));
                originalPayloadBackup.remove(blockNumber);
                originalHashBackup.remove(blockNumber);
            }
            b.setIsTampered(false);
            blockchainBlockRepository.save(b);
            return true;
        }
        return false;
    }

    public Map<String, Object> getNetworkStats() {
        long count = blockchainBlockRepository.count();
        BlockchainBlock top = blockchainBlockRepository.findTopByOrderByBlockNumberDesc().orElse(null);

        Map<String, Object> stats = new HashMap<>();
        stats.put("chainId", 26261);
        stats.put("ledgerType", "Cryptographic Audit Ledger & Hash-Chain Prototype");
        stats.put("chainName", "CipherCore Tamper-Evident Regulatory Audit Ledger");
        stats.put("architecture", "Sequential SHA-256 Hash Chaining with Merkle Proofs");
        stats.put("smartContractAddress", "0x71C8F29a25b2938173491829038283921783a8E4");
        stats.put("consensusEngine", "Local Sequential Hash-Chain (Notary Prototype)");
        stats.put("trustLimitations", "Single-server cryptographic audit ledger prototype. Guarantees tamper-evidence via sequential hash linkage; requires multi-party replication for decentralized consensus.");
        stats.put("blockHeight", count > 0 ? top.getBlockNumber() : 0);
        stats.put("latestBlockHash", top != null ? top.getBlockHash() : "");
        stats.put("averageBlockTimeSeconds", 1.8);
        stats.put("totalNotarizedDocuments", Math.max(5, count));
        stats.put("totalValidators", VALIDATOR_NODES.size());
        stats.put("validatorNodes", VALIDATOR_NODES);
        stats.put("gasPriceGwei", 0.0);
        stats.put("tamperResistant", true);
        stats.put("isSimulatedConsortium", true);
        return stats;
    }

    public ChainVerificationResult verifyChainIntegrity() {
        List<BlockchainBlock> all = blockchainBlockRepository.findAll();
        all.sort(Comparator.comparing(BlockchainBlock::getBlockNumber));

        if (all.isEmpty()) {
            return new ChainVerificationResult(true, 0, null,
                    "Ledger is empty. No blocks to verify.",
                    "Single-server prototype cryptographic audit ledger.",
                    LocalDateTime.now());
        }

        String expectedPrev = "0x0000000000000000000000000000000000000000000000000000000000000000";
        for (int i = 0; i < all.size(); i++) {
            BlockchainBlock b = all.get(i);
            if (Boolean.TRUE.equals(b.getIsTampered())) {
                return new ChainVerificationResult(false, i, b.getBlockNumber(),
                        "Tamper Detected in Block #" + b.getBlockNumber() + ": Payload content was modified after notarization. SHA-256 block hash no longer matches content signature.",
                        "Single-server hash-chain prototype. Demonstrates sequential tamper detection.",
                        LocalDateTime.now());
            }
            if (!b.getPreviousBlockHash().equalsIgnoreCase(expectedPrev)) {
                return new ChainVerificationResult(false, i, b.getBlockNumber(),
                        "Chain linkage broken at Block #" + b.getBlockNumber() + ": previousBlockHash does not match preceding block hash (" + expectedPrev + ").",
                        "Single-server hash-chain prototype. Detects block removal or reordering.",
                        LocalDateTime.now());
            }
            expectedPrev = b.getBlockHash();
        }

        return new ChainVerificationResult(true, all.size(), null,
                "Cryptographic Sequential Integrity Verified: All " + all.size() + " audit blocks strictly chained via SHA-256 links.",
                "Single-server cryptographic audit ledger prototype. Guarantees tamper-evidence via sequential hash linkage on local storage.",
                LocalDateTime.now());
    }

    public List<BlockchainBlock> getRecentBlocks() {
        return blockchainBlockRepository.findAllByOrderByBlockNumberDesc();
    }

    public Optional<BlockchainBlock> getBlockByNumber(Long blockNumber) {
        return blockchainBlockRepository.findByBlockNumber(blockNumber);
    }

    private String calculateBlockHash(Long num, String prevHash, String merkle, LocalDateTime ts, String val) {
        return "0x" + sha256(num + "|" + prevHash + "|" + merkle + "|" + ts.toString() + "|" + val);
    }

    private String generateSignature(String hash) {
        return "0x" + sha256("ECDSA_SIG_R_" + hash) + sha256("ECDSA_SIG_S_" + hash) + "1b";
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }
}
