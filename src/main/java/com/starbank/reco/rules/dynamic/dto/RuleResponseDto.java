package com.starbank.reco.rules.dynamic.dto;

import java.util.List;
import java.util.UUID;

public record RuleResponseDto(UUID id, String product_name, String product_id, String product_text, List<QueryDto> rule) {}
