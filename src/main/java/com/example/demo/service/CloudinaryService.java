package com.example.demo.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Supported file types
    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp",
            "image/webp", "image/svg+xml", "image/tiff");

    private static final List<String> SUPPORTED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/avi", "video/mov", "video/wmv", "video/mkv",
            "video/webm", "video/flv", "video/3gp");

    private static final List<String> SUPPORTED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain", "application/rtf", "application/vnd.oasis.opendocument.text");

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // Validate file type
        if (!isFileTypeSupported(file)) {
            throw new IllegalArgumentException(
                    "Unsupported file type: " + file.getContentType() +
                            ". Supported types: images, videos, and documents (PDF, DOC, DOCX, TXT)");
        }

        // Validate file size (e.g., 20MB limit)
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 20MB limit");
        }

        Map<?, ?> uploadResult = cloudinary.uploader()
                .upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "resource_type", "auto"));
        return uploadResult.get("secure_url").toString();
    }

    @SuppressWarnings("unchecked")
    public void deleteFile(String publicId, String resourceType) throws IOException {
        Map<String, Object> opts = (Map<String, Object>) ObjectUtils.asMap("resource_type", resourceType);
        cloudinary.uploader().destroy(publicId, opts);
    }

    private boolean isFileTypeSupported(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null)
            return false;

        return SUPPORTED_IMAGE_TYPES.contains(contentType) ||
                SUPPORTED_VIDEO_TYPES.contains(contentType) ||
                SUPPORTED_DOCUMENT_TYPES.contains(contentType);
    }

    /**
     * Get a human-readable description of the file type category
     */
    public String getFileTypeCategory(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null)
            return "unknown";

        if (SUPPORTED_IMAGE_TYPES.contains(contentType)) {
            return "image";
        } else if (SUPPORTED_VIDEO_TYPES.contains(contentType)) {
            return "video";
        } else if (SUPPORTED_DOCUMENT_TYPES.contains(contentType)) {
            return "document";
        }
        return "unknown";
    }

    /**
     * Get all supported file types as a formatted string for error messages
     */
    public String getSupportedFileTypesDescription() {
        return "Supported file types: " +
                "Images (JPEG, PNG, GIF, BMP, WebP, SVG, TIFF), " +
                "Videos (MP4, AVI, MOV, WMV, MKV, WebM, FLV, 3GP), " +
                "Documents (PDF, DOC, DOCX, TXT, RTF, ODT)";
    }
}
