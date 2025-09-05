package com.starbank.reco.rules;


import com.starbank.reco.catalog.ProductCatalog;
import com.starbank.reco.dto.ProductRecommendationDto;
import com.starbank.reco.repo.TransactionStatsRepository;
import com.starbank.reco.repo.TxAgg;
import org.springframework.stereotype.Component;


import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Component
public class SimpleCreditRule implements RecommendationRuleSet {
    private final TransactionStatsRepository repo;
    private static final BigDecimal MIN_SPEND = new BigDecimal("100000");


    public SimpleCreditRule(TransactionStatsRepository repo) { this.repo = repo; }


    @Override
    public Optional<ProductRecommendationDto> evaluate(UUID userId) {
        Map<String, TxAgg> agg = repo.fetchAggregatesByProductType(userId);
        boolean hasCredit = agg.containsKey("CREDIT") && agg.get("CREDIT").txCount() > 0;
        var debit = agg.getOrDefault("DEBIT", TxAgg.EMPTY);


        boolean inflowGtOutflow = debit.depositSum().compareTo(debit.spendSum()) > 0;
        boolean spendOver100k = debit.spendSum().compareTo(MIN_SPEND) > 0;


        boolean ok = !hasCredit && inflowGtOutflow && spendOver100k;
        if (!ok) return Optional.empty();


        var p = ProductCatalog.SIMPLE_CREDIT;
        return Optional.of(new ProductRecommendationDto(p.name, p.id, p.text));
    }
}