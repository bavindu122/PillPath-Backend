package com.leo.pillpathbackend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.leo.pillpathbackend.dto.dailymed.DailyMedMediaResponse;
import com.leo.pillpathbackend.dto.dailymed.DailyMedSplPage;
import com.leo.pillpathbackend.service.DailyMedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyMedServiceImpl implements DailyMedService {

    private final WebClient dailyMedClient;

    @Override
    public DailyMedSplPage searchByDrugName(String name, int page, int size) {
        try {
            return dailyMedClient.get()
                    .uri(uri -> uri.path("/spls.json")
                            .queryParam("drug_name", name)
                            .queryParam("name_type", "both")
                            .queryParam("pagesize", size)
                            .queryParam("page", page)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), resp -> resp.createException().map(ex -> {
                        log.warn("DailyMed 4xx search error: {}", ex.getMessage());
                        return new RuntimeException("DailyMed search error: " + ex.getMessage());
                    }))
                    .onStatus(status -> status.is5xxServerError(), resp -> resp.createException().map(ex -> {
                        log.error("DailyMed 5xx search error: {}", ex.getMessage());
                        return new RuntimeException("DailyMed service unavailable");
                    }))
                    .bodyToMono(DailyMedSplPage.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("DailyMed search failed: " + e.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("DailyMed search unexpected error");
        }
    }

    @Override
    public DailyMedMediaResponse getMedia(String setId) {
        try {
            return dailyMedClient.get()
                    .uri("/spls/{setid}/media.json", setId)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), resp -> resp.createException().map(ex -> new RuntimeException("Media not found")))
                    .onStatus(status -> status.is5xxServerError(), resp -> resp.createException().map(ex -> new RuntimeException("DailyMed service unavailable")))
                    .bodyToMono(DailyMedMediaResponse.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch media");
        }
    }

    @Override
    public JsonNode getLabel(String setId) {
        try {
            return dailyMedClient.get()
                    .uri("/spls/{setid}.json", setId)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), resp -> resp.createException().map(ex -> new RuntimeException("Label not found")))
                    .onStatus(status -> status.is5xxServerError(), resp -> resp.createException().map(ex -> new RuntimeException("DailyMed service unavailable")))
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch label");
        }
    }
}
