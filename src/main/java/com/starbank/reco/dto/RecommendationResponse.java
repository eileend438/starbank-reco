package com.starbank.reco.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


public record RecommendationResponse(
        @JsonProperty("user_id") String userId,
        @JsonProperty("recommendations") List<ProductRecommendationDto> recommendations
) {}