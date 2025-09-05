package com.starbank.reco.rules.dynamic;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starbank.reco.dto.ProductRecommendationDto;
import com.starbank.reco.rules.dynamic.dto.QueryDto;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class DynamicRuleService {
    private final DynamicRuleRepository repo;
    private final DynamicRuleEngine engine;
    private final ObjectMapper om = new ObjectMapper();


    public DynamicRuleService(DynamicRuleRepository repo, com.starbank.reco.repo.TransactionStatsRepository knowledgeRepo) {
        this.repo = repo;
        this.engine = new DynamicRuleEngine(knowledgeRepo);
    }


    public List<ProductRecommendationDto> recommend(UUID userId) {
        List<ProductRecommendationDto> out = new ArrayList<>();
        for (DynamicRuleEntity e : repo.findAll()) {
            try {
                var queries = om.readValue(e.getRuleJson(), new TypeReference<List<QueryDto>>(){});
                if (engine.evaluate(userId, queries)) {
                    out.add(new ProductRecommendationDto(e.getProductName(), e.getProductId(), e.getProductText()));
                }
            } catch (Exception ex) { throw new RuntimeException(ex); }
        }
        return out;
    }
}