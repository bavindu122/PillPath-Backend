package com.leo.pillpathbackend.controller;

import com.leo.pillpathbackend.dto.OtcDTO;
import com.leo.pillpathbackend.service.OtcService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/otc")
@AllArgsConstructor
public class OtcController {

    private final OtcService otcService;

    @PostMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<OtcDTO> createOtc(
            @PathVariable Long pharmacyId,
            @Valid @RequestBody OtcDTO otcDto) {
        OtcDTO createdOtc = otcService.createOtcForPharmacy(pharmacyId, otcDto);
        return new ResponseEntity<>(createdOtc, HttpStatus.CREATED);
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


    @PutMapping("/{id}")
    public ResponseEntity<OtcDTO> updateOtc(@PathVariable("id") Long otcId,
                                            @Valid @RequestBody OtcDTO updatedOtcDto) {
        OtcDTO otcDto = otcService.updateOtc(otcId, updatedOtcDto);
        return ResponseEntity.ok(otcDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOtc(@PathVariable("id") Long otcId) {
        otcService.deleteOtc(otcId);
        return ResponseEntity.ok("OTC item deleted successfully.");
    }
}


































// package com.leo.pillpathbackend.controller;

// import com.leo.pillpathbackend.dto.OtcDTO;
// import com.leo.pillpathbackend.service.OtcService;
// import jakarta.validation.Valid;
// import lombok.AllArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/otc")
// @AllArgsConstructor
// public class OtcController {

//     private final OtcService otcService;

//     @PostMapping("/pharmacy/{pharmacyId}")
//     public ResponseEntity<OtcDTO> createOtc(
//             @PathVariable Long pharmacyId,
//             @Valid @RequestBody OtcDTO otcDto) {
//         OtcDTO createdOtc = otcService.createOtcForPharmacy(pharmacyId, otcDto);
//         return new ResponseEntity<>(createdOtc, HttpStatus.CREATED);
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

//     @PutMapping("/{id}")
//     public ResponseEntity<OtcDTO> updateOtc(@PathVariable("id") Long otcId,
//                                             @Valid @RequestBody OtcDTO updatedOtcDto) {
//         OtcDTO otcDto = otcService.updateOtc(otcId, updatedOtcDto);
//         return ResponseEntity.ok(otcDto);
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<String> deleteOtc(@PathVariable("id") Long otcId) {
//         otcService.deleteOtc(otcId);
//         return ResponseEntity.ok("OTC item deleted successfully.");
//     }


//     // Add this endpoint to your OtcController.java
// @GetMapping("/pharmacy/{pharmacyId}")
// public ResponseEntity<List<OtcDTO>> getOtcsByPharmacy(@PathVariable("pharmacyId") Long pharmacyId) {
//     try {
//         List<OtcDTO> otcs = otcService.getOtcsByPharmacy(pharmacyId);
//         return ResponseEntity.ok(otcs);
//     } catch (Exception e) {
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//     }
// }

// @PostMapping("/pharmacy/{pharmacyId}")
// public ResponseEntity<OtcDTO> createOtcForPharmacy(
//         @PathVariable("pharmacyId") Long pharmacyId,
//         @RequestBody OtcDTO otcDTO) {
//     try {
//         OtcDTO createdOtc = otcService.createOtcForPharmacy(pharmacyId, otcDTO);
//         return new ResponseEntity<>(createdOtc, HttpStatus.CREATED);
//     } catch (Exception e) {
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//     }
// }
// }



















// // //package com.leo.pillpathbackend.controller;
// // //
// // //public class OtcController {
// // //}

// // package com.leo.pillpathbackend.controller;

// // import com.leo.pillpathbackend.dto.OtcDTO;
// // import com.leo.pillpathbackend.service.OtcService;
// // import jakarta.validation.Valid;
// // import lombok.AllArgsConstructor;
// // import org.springframework.http.HttpStatus;
// // import org.springframework.http.ResponseEntity;
// // import org.springframework.web.bind.annotation.*;

// // import java.util.List;

// // @RestController
// // @RequestMapping("/api/otc")
// // @AllArgsConstructor
// // public class OtcController {

// //     private final OtcService otcService;

// //     @PostMapping
// //     public ResponseEntity<OtcDTO> createOtc(@Valid @RequestBody OtcDTO otcDto) {
// //         OtcDTO createdOtc = otcService.createOtc(otcDto);
// //         return new ResponseEntity<>(createdOtc, HttpStatus.CREATED);
// //     }

// //     @GetMapping("/{id}")
// //     public ResponseEntity<OtcDTO> getOtcById(@PathVariable("id") Long otcId) {
// //         OtcDTO otcDto = otcService.getOtcById(otcId);
// //         return ResponseEntity.ok(otcDto);
// //     }

// //     @GetMapping
// //     public ResponseEntity<List<OtcDTO>> getAllOtcs() {
// //         List<OtcDTO> otcs = otcService.getAllOtcs();
// //         return ResponseEntity.ok(otcs);
// //     }

// //     @PutMapping("/{id}")
// //     public ResponseEntity<OtcDTO> updateOtc(@PathVariable("id") Long otcId,
// //                                             @Valid @RequestBody OtcDTO updatedOtcDto) {
// //         OtcDTO otcDto = otcService.updateOtc(otcId, updatedOtcDto);
// //         return ResponseEntity.ok(otcDto);
// //     }

// //     @DeleteMapping("/{id}")
// //     public ResponseEntity<String> deleteOtc(@PathVariable("id") Long otcId) {
// //         otcService.deleteOtc(otcId);
// //         return ResponseEntity.ok("OTC item deleted successfully.");
// //     }
// // }