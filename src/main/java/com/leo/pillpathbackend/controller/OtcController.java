package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.OtcDTO;
import com.leo.pillpathbackend.service.CloudinaryService;
import com.leo.pillpathbackend.service.OtcService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/otc")
@AllArgsConstructor
public class OtcController {

    private final OtcService otcService;
    private final CloudinaryService cloudinaryService;

    // @PostMapping("/pharmacy/{pharmacyId}")
    // public ResponseEntity<OtcDTO> createOtc(
    //         @PathVariable Long pharmacyId,
    //         @RequestParam("name") String name,
    //         @RequestParam("description") String description,
    //         @RequestParam("price") Double price,
    //         @RequestParam("stock") Integer stock,
    //         @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

    //     OtcDTO otcDto = new OtcDTO();
    //     otcDto.setName(name);
    //     otcDto.setDescription(description);
    //     otcDto.setPrice(price);
    //     otcDto.setStock(stock);

    //     // Upload image to Cloudinary if provided
    //     if (image != null && !image.isEmpty()) {
    //         Map<String, Object> uploadResult = cloudinaryService.uploadOtcProductImage(image, pharmacyId);
    //         otcDto.setImageUrl((String) uploadResult.get("secure_url"));
    //         otcDto.setImagePublicId((String) uploadResult.get("public_id"));
    //     }

    //     OtcDTO createdOtc = otcService.createOtcForPharmacy(pharmacyId, otcDto);
    //     return new ResponseEntity<>(createdOtc, HttpStatus.CREATED);
    // }

    // @PutMapping("/pharmacy/{pharmacyId}/{id}")
    // public ResponseEntity<OtcDTO> updateOtc(
    //         @PathVariable("id") Long otcId,
    //         @RequestParam("name") String name,
    //         @RequestParam("description") String description,
    //         @RequestParam("price") Double price,
    //         @RequestParam("stock") Integer stock,
    //         @RequestParam(value = "image", required = false) MultipartFile image,
    //         @RequestParam(value = "existingImagePublicId", required = false) String existingImagePublicId,
    //         @RequestParam(value = "existingImageUrl", required = false) String existingImageUrl) throws IOException {

    //     OtcDTO otcDto = new OtcDTO();
    //     otcDto.setName(name);
    //     otcDto.setDescription(description);
    //     otcDto.setPrice(price);
    //     otcDto.setStock(stock);

    //     // Upload new image if provided
    //     if (image != null && !image.isEmpty()) {
    //         // Delete old image from Cloudinary if exists
    //         if (existingImagePublicId != null && !existingImagePublicId.isEmpty()) {
    //             cloudinaryService.deleteImage(existingImagePublicId);
    //         }

    //         // Get pharmacy ID from existing OTC item
    //         OtcDTO existingOtc = otcService.getOtcById(otcId);
            
    //         // Upload new image
    //         Map<String, Object> uploadResult = cloudinaryService.uploadOtcProductImage(image, existingOtc.getPharmacyId());
    //         otcDto.setImageUrl((String) uploadResult.get("secure_url"));
    //         otcDto.setImagePublicId((String) uploadResult.get("public_id"));
    //     } else {
    //         // Keep existing image
    //         otcDto.setImageUrl(existingImageUrl);
    //         otcDto.setImagePublicId(existingImagePublicId);
    //     }

    //     OtcDTO updatedOtc = otcService.updateOtc(otcId, otcDto);
    //     return ResponseEntity.ok(updatedOtc);
    // }


    @PostMapping("/pharmacy/{pharmacyId}")
public ResponseEntity<OtcDTO> createOtc(
        @PathVariable Long pharmacyId,
        @RequestParam("name") String name,
        @RequestParam("description") String description,
        @RequestParam("price") Double price,
        @RequestParam("stock") Integer stock,
        @RequestParam("category") String category,
        @RequestParam("dosage") String dosage,
        @RequestParam("manufacturer") String manufacturer,
        @RequestParam("packSize") String packSize,
        @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

    OtcDTO otcDto = new OtcDTO();
    otcDto.setName(name);
    otcDto.setDescription(description);
    otcDto.setPrice(price);
    otcDto.setStock(stock);
    otcDto.setCategory(category);
    otcDto.setDosage(dosage);
    otcDto.setManufacturer(manufacturer);
    otcDto.setPackSize(packSize);

    // Upload image to Cloudinary if provided
    if (image != null && !image.isEmpty()) {
        Map<String, Object> uploadResult = cloudinaryService.uploadOtcProductImage(image, pharmacyId);
        otcDto.setImageUrl((String) uploadResult.get("secure_url"));
        otcDto.setImagePublicId((String) uploadResult.get("public_id"));
    }

    OtcDTO createdOtc = otcService.createOtcForPharmacy(pharmacyId, otcDto);
    return new ResponseEntity<>(createdOtc, HttpStatus.CREATED);
}

@PutMapping("/pharmacy/{pharmacyId}/{id}")
public ResponseEntity<OtcDTO> updateOtc(
        @PathVariable("id") Long otcId,
        @RequestParam("name") String name,
        @RequestParam("description") String description,
        @RequestParam("price") Double price,
        @RequestParam("stock") Integer stock,
        @RequestParam("category") String category,
        @RequestParam("dosage") String dosage,
        @RequestParam("manufacturer") String manufacturer,
        @RequestParam("packSize") String packSize,
        @RequestParam(value = "image", required = false) MultipartFile image,
        @RequestParam(value = "existingImagePublicId", required = false) String existingImagePublicId,
        @RequestParam(value = "existingImageUrl", required = false) String existingImageUrl) throws IOException {

    OtcDTO otcDto = new OtcDTO();
    otcDto.setName(name);
    otcDto.setDescription(description);
    otcDto.setPrice(price);
    otcDto.setStock(stock);
    otcDto.setCategory(category);
    otcDto.setDosage(dosage);
    otcDto.setManufacturer(manufacturer);
    otcDto.setPackSize(packSize);

    // Upload new image if provided
    if (image != null && !image.isEmpty()) {
        if (existingImagePublicId != null && !existingImagePublicId.isEmpty()) {
            cloudinaryService.deleteImage(existingImagePublicId);
        }

        OtcDTO existingOtc = otcService.getOtcById(otcId);
        Map<String, Object> uploadResult = cloudinaryService.uploadOtcProductImage(image, existingOtc.getPharmacyId());
        otcDto.setImageUrl((String) uploadResult.get("secure_url"));
        otcDto.setImagePublicId((String) uploadResult.get("public_id"));
    } else {
        otcDto.setImageUrl(existingImageUrl);
        otcDto.setImagePublicId(existingImagePublicId);
    }

    OtcDTO updatedOtc = otcService.updateOtc(otcId, otcDto);
    return ResponseEntity.ok(updatedOtc);
}

    @DeleteMapping("/pharmacy/{pharmacyId}/{id}")
    public ResponseEntity<String> deleteOtc(@PathVariable("id") Long otcId) throws IOException {
        // Get the OTC item to retrieve image public ID
        OtcDTO otcDto = otcService.getOtcById(otcId);
        
        // Delete image from Cloudinary if exists
        if (otcDto.getImagePublicId() != null && !otcDto.getImagePublicId().isEmpty()) {
            cloudinaryService.deleteImage(otcDto.getImagePublicId());
        }
        
        otcService.deleteOtc(otcId);
        return ResponseEntity.ok("OTC item deleted successfully.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<OtcDTO> getOtcById(@PathVariable("id") Long otcId) {
        OtcDTO otcDto = otcService.getOtcById(otcId);
        return ResponseEntity.ok(otcDto);
    }

    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<List<OtcDTO>> getOtcsByPharmacy(@PathVariable Long pharmacyId) {
        List<OtcDTO> otcs = otcService.getOtcsByPharmacy(pharmacyId);
        return ResponseEntity.ok(otcs);
    }

    @GetMapping
    public ResponseEntity<List<OtcDTO>> getAllOtcs() {
        List<OtcDTO> otcs = otcService.getAllOtcs();
        return ResponseEntity.ok(otcs);
    }
}






















































// package com.leo.pillpathbackend.controller;

// import com.leo.pillpathbackend.dto.OtcDTO;
// import com.leo.pillpathbackend.service.CloudinaryService;
// import com.leo.pillpathbackend.service.OtcService;
// import lombok.AllArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.IOException;
// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/otc")
// @AllArgsConstructor
// public class OtcController {

//     private final OtcService otcService;
//     private final CloudinaryService cloudinaryService;

//     @PostMapping("/pharmacy/{pharmacyId}")
//     public ResponseEntity<OtcDTO> createOtc(
//             @PathVariable Long pharmacyId,
//             @RequestParam("name") String name,
//             @RequestParam("description") String description,
//             @RequestParam("price") Double price,
//             @RequestParam("stock") Integer stock,
//             @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

//         OtcDTO otcDto = new OtcDTO();
//         otcDto.setName(name);
//         otcDto.setDescription(description);
//         otcDto.setPrice(price);
//         otcDto.setStock(stock);

//         // Upload image to Cloudinary if provided
//         if (image != null && !image.isEmpty()) {
//             Map<String, Object> uploadResult = cloudinaryService.uploadOtcProductImage(image, pharmacyId);
//             otcDto.setImageUrl((String) uploadResult.get("secure_url"));
//             otcDto.setImagePublicId((String) uploadResult.get("public_id"));
//         }

//         OtcDTO createdOtc = otcService.createOtcForPharmacy(pharmacyId, otcDto);
//         return new ResponseEntity<>(createdOtc, HttpStatus.CREATED);
//     }

//     @PutMapping("/pharmacy/{pharmacyId}/{id}")
//     public ResponseEntity<OtcDTO> updateOtc(
//             @PathVariable("id") Long otcId,
//             @RequestParam("name") String name,
//             @RequestParam("description") String description,
//             @RequestParam("price") Double price,
//             @RequestParam("stock") Integer stock,
//             @RequestParam(value = "image", required = false) MultipartFile image,
//             @RequestParam(value = "existingImagePublicId", required = false) String existingImagePublicId,
//             @RequestParam(value = "existingImageUrl", required = false) String existingImageUrl) throws IOException {

//         OtcDTO otcDto = new OtcDTO();
//         otcDto.setName(name);
//         otcDto.setDescription(description);
//         otcDto.setPrice(price);
//         otcDto.setStock(stock);

//         // Upload new image if provided
//         if (image != null && !image.isEmpty()) {
//             // Delete old image from Cloudinary if exists
//             if (existingImagePublicId != null && !existingImagePublicId.isEmpty()) {
//                 cloudinaryService.deleteImage(existingImagePublicId);
//             }

//             // Get pharmacy ID from existing OTC item
//             OtcDTO existingOtc = otcService.getOtcById(otcId);
            
//             // Upload new image
//             Map<String, Object> uploadResult = cloudinaryService.uploadOtcProductImage(image, existingOtc.getPharmacyId());
//             otcDto.setImageUrl((String) uploadResult.get("secure_url"));
//             otcDto.setImagePublicId((String) uploadResult.get("public_id"));
//         } else {
//             // Keep existing image
//             otcDto.setImageUrl(existingImageUrl);
//             otcDto.setImagePublicId(existingImagePublicId);
//         }

//         OtcDTO updatedOtc = otcService.updateOtc(otcId, otcDto);
//         return ResponseEntity.ok(updatedOtc);
//     }

//     @DeleteMapping("/pharmacy/{pharmacyId}/{id}")
//     public ResponseEntity<String> deleteOtc(@PathVariable("id") Long otcId) throws IOException {
//         // Get the OTC item to retrieve image public ID
//         OtcDTO otcDto = otcService.getOtcById(otcId);
        
//         // Delete image from Cloudinary if exists
//         if (otcDto.getImagePublicId() != null && !otcDto.getImagePublicId().isEmpty()) {
//             cloudinaryService.deleteImage(otcDto.getImagePublicId());
//         }
        
//         otcService.deleteOtc(otcId);
//         return ResponseEntity.ok("OTC item deleted successfully.");
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<OtcDTO> getOtcById(@PathVariable("id") Long otcId) {
//         OtcDTO otcDto = otcService.getOtcById(otcId);
//         return ResponseEntity.ok(otcDto);
//     }

//     @GetMapping("/pharmacy/{pharmacyId}")
//     public ResponseEntity<List<OtcDTO>> getOtcsByPharmacy(@PathVariable Long pharmacyId) {
//         List<OtcDTO> otcs = otcService.getOtcsByPharmacy(pharmacyId);
//         return ResponseEntity.ok(otcs);
//     }

//     @GetMapping
//     public ResponseEntity<List<OtcDTO>> getAllOtcs() {
//         List<OtcDTO> otcs = otcService.getAllOtcs();
//         return ResponseEntity.ok(otcs);
//     }
// }
















