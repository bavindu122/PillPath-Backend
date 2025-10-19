package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsChartsDTO {
    private List<PrescriptionUploadsItem> prescriptionUploads;
    private List<PharmacyRegistrationsItem> pharmacyRegistrations;
    private List<GrowthRegistrationsItem> growthRegistrations;
    private List<OrderFulfillmentItem> orderFulfillment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionUploadsItem {
        private String month;   // Jan..Dec
        private int uploads;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PharmacyRegistrationsItem {
        private String month;
        private int registered;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrowthRegistrationsItem {
        private String month;
        private int customers;   // monthly new customers
        private int pharmacies;  // monthly new pharmacy registrations
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderFulfillmentItem {
        private String status; // delivered | pending | cancelled
        private int count;
    }
}

