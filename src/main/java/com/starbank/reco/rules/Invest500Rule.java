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
public class Invest500Rule implements RecommendationRuleSet {
    private final TransactionStatsRepository repo;
    private static final BigDecimal SAVING_MIN = new BigDecimal("1000");


    public Invest500Rule(TransactionStatsRepository repo) { this.repo = repo; }


    @Override
    public Optional<ProductRecommendationDto> evaluate(UUID userId) {
        Map<String, TxAgg> agg = repo.fetchAggregatesByProductType(userId);
        boolean hasDebit = agg.containsKey("DEBIT") && agg.get("DEBIT").txCount() > 0;
        boolean hasInvest = agg.containsKey("INVEST") && agg.get("INVEST").txCount() > 0;
        BigDecimal savingDeposit = agg.getOrDefault("SAVING", TxAgg.EMPTY).depositSum();


        boolean ok = hasDebit && !hasInvest && savingDeposit.compareTo(SAVING_MIN) > 0;
        if (!ok) return Optional.empty();


        var p = ProductCatalog.INVEST_500;
        return Optional.of(new ProductRecommendationDto(p.name, p.id, p.text));
    }
}