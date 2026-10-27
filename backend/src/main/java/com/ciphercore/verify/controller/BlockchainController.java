package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.BlockchainBlock;
import com.ciphercore.verify.service.BlockchainNotaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blockchain")
@CrossOrigin(origins = "*")
public class BlockchainController {

    private final BlockchainNotaryService blockchainNotaryService;

    public BlockchainController(BlockchainNotaryService blockchainNotaryService) {
        this.blockchainNotaryService = blockchainNotaryService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNetworkStats() {
        return ResponseEntity.ok(blockchainNotaryService.getNetworkStats());
    }

    @GetMapping("/blocks")
    public ResponseEntity<List<BlockchainBlock>> getRecentBlocks() {
        return ResponseEntity.ok(blockchainNotaryService.getRecentBlocks());
    }

    @GetMapping("/blocks/{blockNumber}")
    public ResponseEntity<?> getBlock(@PathVariable Long blockNumber) {
        return blockchainNotaryService.getBlockByNumber(blockNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record NotarizeDocumentRequest(
            String documentTitle,
            String documentHash,
            String institutionCode,
            String institutionName,
            String docCategory,
            String notaryOfficer
    ) {}

    @PostMapping("/notarize")
    public ResponseEntity<BlockchainNotaryService.NotarizationResult> notarize(
            @RequestBody NotarizeDocumentRequest req) {
        return ResponseEntity.ok(blockchainNotaryService.notarizeDocument(
                req.documentTitle(),
                req.documentHash(),
                req.institutionCode(),
                req.institutionName(),
                req.docCategory(),
                req.notaryOfficer()
        ));
    }

    @GetMapping("/verify/{docHash}")
    public ResponseEntity<BlockchainNotaryService.VerificationResult> verify(
            @PathVariable String docHash) {
        return ResponseEntity.ok(blockchainNotaryService.verifyDocumentOnChain(docHash));
    }

    @GetMapping("/verify-chain")
    public ResponseEntity<BlockchainNotaryService.ChainVerificationResult> verifyChain() {
        return ResponseEntity.ok(blockchainNotaryService.verifyChainIntegrity());
    }

    public record BlockchainTamperRequest(Long blockNumber) {}

    @PostMapping("/simulate-tamper")
    public ResponseEntity<?> simulateTamper(@RequestBody BlockchainTamperRequest req) {
        boolean ok = blockchainNotaryService.simulateTamper(req.blockNumber());
        if (ok) {
            return ResponseEntity.ok(Map.of(
                    "status", "AUDIT_LEDGER_TAMPER_SIMULATED",
                    "blockNumber", req.blockNumber(),
                    "message", "Tamper Detected: Block #" + req.blockNumber() +
                               " payload modified. SHA-256 block hash no longer matches content signature. Sequential chain linkage broken.",
                    "ledgerType", "Cryptographic Hash-Chain Audit Ledger Prototype"
            ));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Block #" + req.blockNumber() + " not found"));
    }

    @PostMapping("/restore-tamper")
    public ResponseEntity<?> restoreTamper(@RequestBody BlockchainTamperRequest req) {
        boolean ok = blockchainNotaryService.restoreTamper(req.blockNumber());
        if (ok) {
            return ResponseEntity.ok(Map.of(
                    "status", "AUDIT_LEDGER_INTEGRITY_RESTORED",
                    "blockNumber", req.blockNumber(),
                    "message", "Cryptographic Integrity Restored: Block #" + req.blockNumber() +
                               " payload restored to original SHA-256 hash. Sequential chain verified.",
                    "ledgerType", "Cryptographic Hash-Chain Audit Ledger Prototype"
            ));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Block #" + req.blockNumber() + " not found"));
    }
}
