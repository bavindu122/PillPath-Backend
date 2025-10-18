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

    private Map<String, Object> uploadImageWithValidation(
            MultipartFile file,
            String publicId,
            Transformation transformation,
            long maxSizeBytes) throws IOException {

        // Common validation
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File size exceeds limit");
        }

        // Common upload logic
        return cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "image",
                        "transformation", transformation
                )
        );
    }

    @Override
    public Map<String, Object> uploadProfilePicture(MultipartFile file, Long userId) throws IOException {
        String publicId = "pillpath/profile-pics/" + userId + "-" + UUID.randomUUID();
        Transformation transformation = new Transformation()
                .width(400).height(400).crop("fill").gravity("face")
                .quality("auto").fetchFormat("auto");

        return uploadImageWithValidation(file, publicId, transformation, 5 * 1024 * 1024);
    }

    //pharmacy
    //pharmacy logo upload (square)
    @Override
    public Map<String, Object> uploadPharmacyLogo(MultipartFile file, Long pharmacyId) throws IOException {
        String publicId = "pillpath/pharmacies/logos/" + pharmacyId + "-" + UUID.randomUUID();
        Transformation transformation = new Transformation()
                .width(512).height(512).crop("fill").gravity("auto")
                .quality("auto").fetchFormat("auto");

        return uploadImageWithValidation(file, publicId, transformation, 5 * 1024 * 1024);
    }

    // New: pharmacy banner upload (wide)
    @Override
    public Map<String, Object> uploadPharmacyBanner(MultipartFile file, Long pharmacyId) throws IOException {
        String publicId = "pillpath/pharmacies/banners/" + pharmacyId + "-" + UUID.randomUUID();
        Transformation transformation = new Transformation()
                .width(1600).height(500).crop("fill").gravity("auto")
                .quality("auto").fetchFormat("auto");

        return uploadImageWithValidation(file, publicId, transformation, 8 * 1024 * 1024);
    }

    @Override
    public Map<String, Object> uploadPrescriptionImage(MultipartFile file, Long customerId, Long pharmacyId) throws IOException {
        String publicId = "pillpath/prescriptions/" + pharmacyId + "/" + customerId + "-" + UUID.randomUUID();
        Transformation transformation = new Transformation()
                .width(1600).height(1600).crop("limit")
                .quality("auto").fetchFormat("auto");

        // 8MB limit for prescription images
        return uploadImageWithValidation(file, publicId, transformation, 8 * 1024 * 1024);
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

    // Add this method to your existing CloudinaryServiceImpl class

    @Override
    public Map<String, Object> uploadOtcProductImage(MultipartFile file, Long pharmacyId) throws IOException {
            String publicId = "pillpath/otc-products/" + pharmacyId + "-" + UUID.randomUUID();
            Transformation transformation = new Transformation()
                    .width(800).height(800).crop("fill").gravity("auto")
                    .quality("auto").fetchFormat("auto");

    // 5MB limit for OTC product images
    return uploadImageWithValidation(file, publicId, transformation, 5 * 1024 * 1024);
}
}