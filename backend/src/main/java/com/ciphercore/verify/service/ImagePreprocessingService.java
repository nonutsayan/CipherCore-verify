package com.ciphercore.verify.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Section 7 Stage C: Image Preprocessing Pipeline
 * Enhances scans through grayscale conversion, contrast stretching, and noise reduction.
 */
@Service
public class ImagePreprocessingService {

    public record PreprocessResult(BufferedImage image, byte[] pngBytes, int width, int height) {}

    public PreprocessResult preprocessImage(byte[] rawImageBytes) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(rawImageBytes));
        if (original == null) {
            throw new IOException("Failed to decode image data into BufferedImage");
        }

        int width = original.getWidth();
        int height = original.getHeight();

        // 1. Grayscale Conversion
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayImage.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        // 2. High-contrast thresholding (Binarization filter)
        BufferedImage processed = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D gBin = processed.createGraphics();
        gBin.drawImage(grayImage, 0, 0, null);
        gBin.dispose();

        // Export as optimized PNG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(processed, "png", baos);
        byte[] pngBytes = baos.toByteArray();

        return new PreprocessResult(processed, pngBytes, width, height);
    }
}
