package com.leo.pillpathbackend.dto;

import com.leo.pillpathbackend.entity.Announcement;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class AddAnnouncementResponse{
    private String message;
    private Announcement announcement;
}
