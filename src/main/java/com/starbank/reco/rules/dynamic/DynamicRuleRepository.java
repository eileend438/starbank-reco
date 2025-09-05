package com.starbank.reco.rules.dynamic;


import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;


public interface DynamicRuleRepository extends JpaRepository<DynamicRuleEntity, UUID> {
    Optional<DynamicRuleEntity> findByProductId(String productId);
    void deleteByProductId(String productId);
}