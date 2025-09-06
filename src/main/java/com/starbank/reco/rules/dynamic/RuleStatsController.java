package com.starbank.reco.rules.dynamic;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rule")
public class RuleStatsController {

    private final DynamicRuleRepository ruleRepo;
    private final RuleStatRepository statRepo;

    public RuleStatsController(DynamicRuleRepository ruleRepo, RuleStatRepository statRepo) {
        this.ruleRepo = ruleRepo;
        this.statRepo = statRepo;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        var allRules = ruleRepo.findAll();
        var statsMap = statRepo.findAll().stream()
                .collect(Collectors.toMap(RuleStatEntity::getRuleId, RuleStatEntity::getCount));

        var list = new ArrayList<Map<String, String>>();
        for (var r : allRules) {
            long cnt = statsMap.getOrDefault(r.getId(), 0L);
            list.add(Map.of(
                    "rule_id", r.getId().toString(),
                    "count", String.valueOf(cnt)
            ));
        }
        return Map.of("stats", list);
    }
}
