package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) throws IOException {
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
}
