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
public class TopSavingRule implements RecommendationRuleSet {
    private final TransactionStatsRepository repo;
    private static final BigDecimal THRESHOLD = new BigDecimal("50000");


    public TopSavingRule(TransactionStatsRepository repo) { this.repo = repo; }


    @Override
    public Optional<ProductRecommendationDto> evaluate(UUID userId) {
        Map<String, TxAgg> agg = repo.fetchAggregatesByProductType(userId);
        boolean hasDebit = agg.containsKey("DEBIT") && agg.get("DEBIT").txCount() > 0;
        var debit = agg.getOrDefault("DEBIT", TxAgg.EMPTY);
        var saving = agg.getOrDefault("SAVING", TxAgg.EMPTY);


        boolean deposit50k = debit.depositSum().compareTo(THRESHOLD) >= 0
                || saving.depositSum().compareTo(THRESHOLD) >= 0;
        boolean inflowGtOutflow = debit.depositSum().compareTo(debit.spendSum()) > 0;


        boolean isCondition = hasDebit && deposit50k && inflowGtOutflow;
        if (!isCondition) return Optional.empty();


        var p = ProductCatalog.TOP_SAVING;
        return Optional.of(new ProductRecommendationDto(p.name, p.id, p.text));
    }
}