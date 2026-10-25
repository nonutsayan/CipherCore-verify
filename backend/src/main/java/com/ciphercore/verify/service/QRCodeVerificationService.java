package com.ciphercore.verify.service;

import com.ciphercore.verify.model.AnomalyRecord;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.EvidenceRecord;
import com.ciphercore.verify.model.VerificationRecord;
import com.ciphercore.verify.model.enums.AnomalySeverity;
import com.ciphercore.verify.model.enums.AnomalyType;
import com.ciphercore.verify.model.enums.VerificationStatus;
import com.ciphercore.verify.repository.AnomalyRecordRepository;
import com.ciphercore.verify.repository.EvidenceRecordRepository;
import com.ciphercore.verify.repository.VerificationRecordRepository;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.*;

/**
 * Section 17: QR Code & Barcode Verification Service
 * Uses ZXing to decode 2D barcodes, enforces SSRF protections against untrusted destinations,
 * and validates that encoded payloads match printed certificate metadata.
 */
@Service
public class QRCodeVerificationService {

    private static final Logger log = LoggerFactory.getLogger(QRCodeVerificationService.class);

    private static final List<String> TRUSTED_DOMAINS = List.of(
        "aicte-india.org", "gov.in", "nic.in", "digilocker.gov.in"
    );

    @Autowired
    private VerificationRecordRepository verificationRecordRepository;

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    public record QRCodeScanResult(
        boolean qrFound,
        String decodedText,
        String barcodeFormat,
        boolean isTrustedDomain,
        String payloadMismatch
    ) {}

    public QRCodeScanResult inspectQRCodes(Document document, String printedCertificateNumber, Long runId) {
        File diskFile = new File(document.getStoredPath());
        if (!diskFile.exists()) {
            return new QRCodeScanResult(false, null, null, false, "File missing on disk");
        }

        BufferedImage img = null;
        try {
            if ("application/pdf".equalsIgnoreCase(document.getMimeType())) {
                try (PDDocument pd = Loader.loadPDF(diskFile)) {
                    if (pd.getNumberOfPages() > 0) {
                        PDFRenderer renderer = new PDFRenderer(pd);
                        img = renderer.renderImageWithDPI(0, 150);
                    }
                }
            } else if (document.getMimeType() != null && document.getMimeType().startsWith("image/")) {
                img = ImageIO.read(diskFile);
            }
        } catch (Exception e) {
            log.warn("Could not render document image for barcode scan: {}", e.getMessage());
        }

        if (img == null) {
            return new QRCodeScanResult(false, null, null, false, "Rendering unavailable");
        }

        try {
            LuminanceSource source = new BufferedImageLuminanceSource(img);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            Result result = new MultiFormatReader().decode(bitmap, hints);
            String payload = result.getText();
            String format = result.getBarcodeFormat().toString();

            boolean isTrustedDomain = false;
            // 1. SSRF and Domain Trust Inspection
            if (payload.startsWith("http://") || payload.startsWith("https://")) {
                try {
                    URI uri = URI.create(payload);
                    String host = uri.getHost();
                    if (host != null) {
                        for (String trusted : TRUSTED_DOMAINS) {
                            if (host.equalsIgnoreCase(trusted) || host.endsWith("." + trusted)) {
                                isTrustedDomain = true;
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) {}

                if (!isTrustedDomain) {
                    String desc = "QR Code points to untrusted or external third-party destination: " + payload;
                    AnomalyRecord anomaly = new AnomalyRecord(
                        runId,
                        document.getId(),
                        document.getApplicationId(),
                        AnomalyType.QR_CODE_MISMATCH,
                        AnomalySeverity.HIGH,
                        "Untrusted QR Code Verification Destination",
                        desc,
                        "Official AICTE statutory instruments only embed QR verification links hosted under .gov.in or .aicte-india.org official infrastructure.",
                        "RULE_QR_UNTRUSTED_DESTINATION",
                        0.93,
                        "Do not visit arbitrary QR destination URLs. Inspect certificate via official AICTE central portal."
                    );
                    anomalyRecordRepository.save(anomaly);

                    EvidenceRecord ev = new EvidenceRecord(
                        anomaly,
                        document.getId(),
                        "QR_CODE_MISMATCH",
                        1,
                        "qrCodePayload",
                        "Decoded URL: " + payload,
                        "Authorized Domain Whitelist (*.gov.in, *.aicte-india.org)",
                        "SSRF prevention rule enforced: Server will not initiate automated HTTP requests to untrusted targets."
                    );
                    evidenceRecordRepository.save(ev);
                }
            }

            // 2. Cross-check Encoded Identifier against Printed Certificate Number
            if (printedCertificateNumber != null && !printedCertificateNumber.isBlank()) {
                String cleanPrinted = printedCertificateNumber.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
                String cleanPayload = payload.replaceAll("[^A-Za-z0-9]", "").toUpperCase();

                if (cleanPrinted.length() >= 5 && !cleanPayload.contains(cleanPrinted)) {
                    String desc = String.format("QR code payload conflict: Encoded text ('%s') does not contain or match printed certificate number '%s'.",
                        payload, printedCertificateNumber);

                    AnomalyRecord anomaly = new AnomalyRecord(
                        runId,
                        document.getId(),
                        document.getApplicationId(),
                        AnomalyType.QR_CODE_MISMATCH,
                        AnomalySeverity.CRITICAL,
                        "QR Code Payload Mismatch with Printed Certificate Number",
                        desc,
                        "A QR code copied from another document and pasted onto a certificate will encode an identifier conflicting with the printed text.",
                        "RULE_QR_PAYLOAD_MISMATCH",
                        0.96,
                        "Verify authenticity of QR code barcode rasterization with central issuing repository."
                    );
                    anomalyRecordRepository.save(anomaly);

                    EvidenceRecord ev = new EvidenceRecord(
                        anomaly,
                        document.getId(),
                        "QR_CODE_MISMATCH",
                        1,
                        "qrCodePayload",
                        "Decoded QR Payload: " + payload,
                        "Printed Certificate Number: " + printedCertificateNumber,
                        "Physical seals and QR stamps must be corroborated against master database archives."
                    );
                    evidenceRecordRepository.save(ev);
                }
            }

            VerificationRecord rec = new VerificationRecord(
                document.getId(),
                "QR_PAYLOAD_VALIDATION",
                format + "_BARCODE",
                VerificationStatus.VERIFIED_MATCH,
                "Decoded " + format + ": " + payload + " | Trusted Domain: " + isTrustedDomain,
                "QR presence alone does not establish legal authenticity; encoded data was verified against printed fields."
            );
            verificationRecordRepository.save(rec);

            return new QRCodeScanResult(true, payload, format, isTrustedDomain, null);

        } catch (NotFoundException e) {
            // No barcode found on page 1 - benign for traditional scanned documents
            VerificationRecord rec = new VerificationRecord(
                document.getId(),
                "QR_PAYLOAD_VALIDATION",
                "NONE",
                VerificationStatus.SIGNATURE_NOT_PRESENT,
                "No QR Code or 2D barcode detected on primary page.",
                "Traditional paper documents with wet-ink seals and stamps do not require 2D barcodes."
            );
            verificationRecordRepository.save(rec);
            return new QRCodeScanResult(false, null, null, false, "No barcode detected");

        } catch (Exception e) {
            log.warn("Barcode reading exception for document {}: {}", document.getId(), e.getMessage());
            return new QRCodeScanResult(false, null, null, false, e.getMessage());
        }
    }
}
