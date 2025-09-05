package com.starbank.reco.dto;


import com.fasterxml.jackson.annotation.JsonProperty;


public record ProductRecommendationDto(
        @JsonProperty("name") String name,
        @JsonProperty("id") String id,
        @JsonProperty("text") String text
) {}