package com.starbank.reco.service;


import com.starbank.reco.dto.ProductRecommendationDto;
import com.starbank.reco.rules.RecommendationRuleSet;
import com.starbank.reco.rules.dynamic.DynamicRuleService;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class RecommendationService {
    private final List<RecommendationRuleSet> staticRules;
    private final DynamicRuleService dynamicRuleService;


    public RecommendationService(List<RecommendationRuleSet> staticRules, DynamicRuleService dynamicRuleService) {
        this.staticRules = staticRules;
        this.dynamicRuleService = dynamicRuleService;
    }


    public List<ProductRecommendationDto> recommend(UUID userId) {
        List<ProductRecommendationDto> out = new ArrayList<>();

        out.addAll(dynamicRuleService.recommend(userId));

        for (RecommendationRuleSet r : staticRules) r.evaluate(userId).ifPresent(out::add);
        return out;
    }
}