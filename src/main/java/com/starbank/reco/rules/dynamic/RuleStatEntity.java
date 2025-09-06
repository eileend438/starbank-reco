package com.starbank.reco.rules.dynamic;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "rule_stat")
public class RuleStatEntity {
    @Id
    @Column(name = "rule_id", length = 36, nullable = false)
    private UUID ruleId;

    @Column(name = "count", nullable = false)
    private long count;

    public RuleStatEntity() {}
    public RuleStatEntity(UUID ruleId, long count) { this.ruleId = ruleId; this.count = count; }

    public UUID getRuleId() { return ruleId; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
