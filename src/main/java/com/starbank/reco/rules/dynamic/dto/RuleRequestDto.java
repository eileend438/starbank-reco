package com.starbank.reco.rules.dynamic.dto;

import java.util.List;

public record RuleRequestDto(String product_name, String product_id, String product_text, List<QueryDto> rule) {}
