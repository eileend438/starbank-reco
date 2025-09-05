package com.starbank.reco.controller;

import com.starbank.reco.dto.ProductRecommendationDto;
import com.starbank.reco.dto.RecommendationResponse;
import com.starbank.reco.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/recommendation")
public class RecommendationController {
    private final RecommendationService service;


    public RecommendationController(RecommendationService service) {
        this.service = service;
    }


    @GetMapping("/{userId}")
    public ResponseEntity<RecommendationResponse> get(@PathVariable String userId) {
        try {
            UUID uuid = UUID.fromString(userId);
            List<ProductRecommendationDto> recos = service.recommend(uuid);
            return ResponseEntity.ok(new RecommendationResponse(userId, recos));
        } catch (IllegalArgumentException badUuid) {
            return ResponseEntity.badRequest().build();
        }
    }
}