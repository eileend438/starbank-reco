package com.starbank.reco.controller;

import com.starbank.reco.dto.ProductRecommendationDto;
import com.starbank.reco.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    RecommendationService recommendationService;

    @Test
    void shouldReturnRecommendations() throws Exception {
        UUID userId = UUID.randomUUID();
        when(recommendationService.recommend(userId))
                .thenReturn(List.of(
                        new ProductRecommendationDto("Invest 500", "147f6a0f-3b91-413b-ab99-87f081d60d5a", "text")
                ));

        mvc.perform(get("/recommendation/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.user_id").value(userId.toString()))
                .andExpect(jsonPath("$.recommendations[0].name").value("Invest 500"))
                .andExpect(jsonPath("$.recommendations[0].id").value("147f6a0f-3b91-413b-ab99-87f081d60d5a"));
    }

    @Test
    void shouldReturnEmptyListIfNoRecommendations() throws Exception {
        UUID userId = UUID.randomUUID();
        when(recommendationService.recommend(userId)).thenReturn(List.of());

        mvc.perform(get("/recommendation/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(userId.toString()))
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations").isEmpty());
    }
}
