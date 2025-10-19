package com.leo.pillpathbackend.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    //customer
    Map<String, Object> uploadProfilePicture(MultipartFile file, Long userId) throws IOException;
    void deleteImage(String publicId) throws IOException;
    @Deprecated
    String extractPublicIdFromUrl(String imageUrl);
    //pharmacy
    Map<String, Object> uploadPharmacyLogo(MultipartFile file, Long pharmacyId) throws IOException;
    Map<String, Object> uploadPharmacyBanner(MultipartFile file, Long pharmacyId) throws IOException;

    // prescriptions
    Map<String, Object> uploadPrescriptionImage(MultipartFile file, Long customerId, Long pharmacyId) throws IOException;

    // payouts receipts (images or PDFs)
    Map<String, Object> uploadPayoutReceipt(MultipartFile file) throws IOException;
}