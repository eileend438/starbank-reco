package com.starbank.reco.rules.dynamic;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dynamic_rule")
public class DynamicRuleEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private UUID id;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Lob
    @Column(name = "product_text")
    private String productText;

    @Lob
    @Column(name = "rule_json", nullable = false)
    private String ruleJson;

    @Column(name = "created_at")
    private Instant createdAt;

    public DynamicRuleEntity() {}

    public DynamicRuleEntity(UUID id, String productName, String productId, String productText, String ruleJson, Instant createdAt) {
        this.id = id;
        this.productName = productName;
        this.productId = productId;
        this.productText = productText;
        this.ruleJson = ruleJson;
        this.createdAt = createdAt;
    }

public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductText() { return productText; }
    public void setProductText(String productText) { this.productText = productText; }
    public String getRuleJson() { return ruleJson; }
    public void setRuleJson(String ruleJson) { this.ruleJson = ruleJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}