package com.starbank.reco.rules.dynamic;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starbank.reco.rules.dynamic.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


import java.time.Instant;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/rule")
public class RuleController {
    private final DynamicRuleRepository repo;
    private final ObjectMapper om = new ObjectMapper();


    public RuleController(DynamicRuleRepository repo) { this.repo = repo; }


    @PostMapping
    @Transactional
    public ResponseEntity<RuleResponseDto> upsert(@RequestBody RuleRequestDto dto) throws Exception {

        if (dto.product_id() == null || dto.product_id().isBlank()) return ResponseEntity.badRequest().build();
        if (dto.product_name() == null || dto.product_name().isBlank()) return ResponseEntity.badRequest().build();
        if (dto.rule() == null || dto.rule().isEmpty()) return ResponseEntity.badRequest().build();



        String json = om.writeValueAsString(dto.rule());


        DynamicRuleEntity e = repo.findByProductId(dto.product_id())
                .orElseGet(() -> new DynamicRuleEntity(UUID.randomUUID(), dto.product_name(), dto.product_id(), dto.product_text(), json, Instant.now()));
        e.setProductName(dto.product_name());
        e.setProductText(dto.product_text());
        e.setRuleJson(json);
        repo.save(e);


        List<QueryDto> rule = om.readValue(e.getRuleJson(), new TypeReference<List<QueryDto>>(){});
        return ResponseEntity.ok(new RuleResponseDto(e.getId(), e.getProductName(), e.getProductId(), e.getProductText(), rule));
    }


    @GetMapping
    public RuleListResponseDto list() throws Exception {
        return new RuleListResponseDto(
                repo.findAll().stream().map(e -> {
                    try {
                        List<QueryDto> rule = om.readValue(e.getRuleJson(), new TypeReference<List<QueryDto>>(){});
                        return new RuleResponseDto(e.getId(), e.getProductName(), e.getProductId(), e.getProductText(), rule);
                    } catch (Exception ex) { throw new RuntimeException(ex); }
                }).toList()
        );
    }


    @DeleteMapping("/{productId}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable String productId) {
        repo.deleteByProductId(productId);
        return ResponseEntity.noContent().build();
    }
}