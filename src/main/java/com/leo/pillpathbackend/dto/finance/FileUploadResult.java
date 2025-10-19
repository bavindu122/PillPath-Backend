package com.leo.pillpathbackend.dto.finance;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResult {
    private String url;
    private String fileName;
    private String fileType;
    private long size;
}

