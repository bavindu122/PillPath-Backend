package com.leo.pillpathbackend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.leo.pillpathbackend.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public Map<String, Object> uploadProfilePicture(MultipartFile file, Long userId) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Check file size (5MB limit)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }

        // Generate unique filename
        String publicId = "pillpath/profile-pics/" + userId + "-" + UUID.randomUUID();

        System.out.println("=== CLOUDINARY UPLOAD DEBUG ===");
        System.out.println("File name: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize() + " bytes");
        System.out.println("Content type: " + contentType);
        System.out.println("Generated publicId: " + publicId);
        System.out.println("User ID: " + userId);

        try {
            // Upload with transformations
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "image",
                            "transformation", new Transformation()
                                    .width(400).height(400).crop("fill").gravity("face")
                                    .quality("auto").fetchFormat("auto")
                    )
            );

            System.out.println("=== UPLOAD SUCCESSFUL ===");
            System.out.println("Secure URL: " + uploadResult.get("secure_url"));
            System.out.println("Public ID: " + uploadResult.get("public_id"));
            System.out.println("Version: " + uploadResult.get("version"));
            System.out.println("Format: " + uploadResult.get("format"));
            System.out.println("Resource Type: " + uploadResult.get("resource_type"));
            System.out.println("Full response: " + uploadResult);

            return uploadResult;

        } catch (Exception e) {
            System.err.println("=== UPLOAD FAILED ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void deleteImage(String publicId) {
        try {
            Map<String, Object> params = Map.of(
                    "invalidate", false,
                    "resource_type", "image"
            );
            cloudinary.uploader().destroy(publicId, params);
        } catch (Exception e) {
            // Log the error but don't fail the upload
            System.err.println("Failed to delete old image: " + e.getMessage());
        }
    }

    @Deprecated
    @Override
    public String extractPublicIdFromUrl(String imageUrl) {
        // Keep for legacy/compatibility, but avoid using in new code
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            String[] parts = imageUrl.split("/");
            String filename = parts[parts.length - 1];
            return "pillpath/profile-pics/" + filename.substring(0, filename.lastIndexOf('.'));
        } catch (Exception e) {
            return null;
        }
    }
}