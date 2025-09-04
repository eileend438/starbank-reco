package com.starbank.reco.rules;


import com.starbank.reco.dto.ProductRecommendationDto;


import java.util.Optional;
import java.util.UUID;


public interface RecommendationRuleSet {
    Optional<ProductRecommendationDto> evaluate(UUID userId);
}