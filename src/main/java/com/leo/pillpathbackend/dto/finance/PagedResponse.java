package com.leo.pillpathbackend.dto.finance;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {
    private List<T> items;
    private int page; // 1-based
    private int size;
    private long total;
}

