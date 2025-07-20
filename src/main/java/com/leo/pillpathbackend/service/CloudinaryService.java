package com.leo.pillpathbackend.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    Map<String, Object> uploadProfilePicture(MultipartFile file, Long userId) throws IOException;
    void deleteImage(String publicId) throws IOException;
    String extractPublicIdFromUrl(String imageUrl);
}