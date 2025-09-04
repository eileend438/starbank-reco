package com.starbank.reco.rules.dynamic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starbank.reco.dto.ProductRecommendationDto;
import com.starbank.reco.repo.TransactionStatsRepository;
import com.starbank.reco.rules.dynamic.dto.QueryDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DynamicRuleService {

    private final DynamicRuleRepository repo;
    private final DynamicRuleEngine engine;
    private final RuleStatService ruleStatService;
    private final ObjectMapper om;

    public DynamicRuleService(DynamicRuleRepository repo,
                              TransactionStatsRepository knowledgeRepo,
                              RuleStatService ruleStatService,
                              ObjectMapper om
    ) {
        this.repo = repo;
        this.engine = new DynamicRuleEngine(knowledgeRepo);
        this.ruleStatService = ruleStatService;
        this.om = om;
    }

    public List<ProductRecommendationDto> recommend(UUID userId) {
        List<ProductRecommendationDto> out = new ArrayList<>();
        for (DynamicRuleEntity e : repo.findAll()) {
            try {
                var queries = om.readValue(e.getRuleJson(), new TypeReference<List<QueryDto>>() {});
                if (engine.evaluate(userId, queries)) {

                    out.add(new ProductRecommendationDto(e.getProductName(), e.getProductId(), e.getProductText()));

                    ruleStatService.increment(e.getId());
                }
            } catch (Exception ex) {
                throw new RuntimeException("Failed to evaluate dynamic rule " + e.getId(), ex);
            }
        }
        return out;
    }
}
