package com.starbank.reco.rules.dynamic;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RuleStatService {
    private final RuleStatRepository repo;

    public RuleStatService(RuleStatRepository repo) { this.repo = repo; }

    @Transactional
    public void increment(UUID ruleId) {
        var stat = repo.findById(ruleId).orElseGet(() -> new RuleStatEntity(ruleId, 0));
        stat.setCount(stat.getCount() + 1);
        repo.save(stat);
    }
}
