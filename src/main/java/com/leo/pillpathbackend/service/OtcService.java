package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.OtcDTO;
import java.util.List;

public interface OtcService {

    OtcDTO createOtcForPharmacy(Long pharmacyId, OtcDTO otcDTO);

    OtcDTO getOtcById(Long otcId);

    List<OtcDTO> getAllOtcs();

    List<OtcDTO> getOtcsByPharmacy(Long pharmacyId);

    OtcDTO updateOtc(Long otcId, OtcDTO otcDTO);

    void deleteOtc(Long otcId);
}






// //package com.leo.pillpathbackend.service;
// //
// //public interface OtcService {
// //}

// package com.leo.pillpathbackend.service;

// import com.leo.pillpathbackend.dto.OtcDTO;
// import java.util.List;

// public interface OtcService {

//     OtcDTO createOtc(OtcDTO otcDTO);

//     OtcDTO getOtcById(Long otcId);

//     List<OtcDTO> getAllOtcs();

//     OtcDTO updateOtc(Long otcId, OtcDTO otcDTO);

//     void deleteOtc(Long otcId);
// }