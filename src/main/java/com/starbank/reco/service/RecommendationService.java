package com.starbank.reco.service;


import com.starbank.reco.dto.ProductRecommendationDto;
import com.starbank.reco.rules.RecommendationRuleSet;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class RecommendationService {
    private final List<RecommendationRuleSet> rules;


    public RecommendationService(List<RecommendationRuleSet> rules) {
        this.rules = rules;
    }


    public List<ProductRecommendationDto> recommend(UUID userId) {
        List<ProductRecommendationDto> out = new ArrayList<>();
        for (var r : rules) {
            r.evaluate(userId).ifPresent(out::add);
        }
        return out;
    }
}