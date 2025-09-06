package com.starbank.reco.rules.dynamic.dto;

import java.util.List;

public record QueryDto(String query, List<String> arguments, boolean negate) {}
