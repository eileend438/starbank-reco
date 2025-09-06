package com.starbank.reco.rules.dynamic;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RuleStatRepository extends JpaRepository<RuleStatEntity, UUID> {}
