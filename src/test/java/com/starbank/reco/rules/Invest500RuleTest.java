package com.starbank.reco.rules;

import com.starbank.reco.dto.ProductRecommendationDto;
import com.starbank.reco.repo.TransactionStatsRepository;
import com.starbank.reco.repo.TxAgg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Invest500RuleTest {

    @Mock
    TransactionStatsRepository repo;

    Invest500Rule rule;

    @BeforeEach
    void setUp() {
        rule = new Invest500Rule(repo);
    }

    @Test
    void shouldRecommendWhenConditionsMet() {
        UUID user = UUID.randomUUID();
        Map<String, TxAgg> agg = new HashMap<>();
        // есть дебит с ≥1 транзакцией
        agg.put("DEBIT", new TxAgg(new BigDecimal("10"), BigDecimal.ZERO, 2));
        // нет инвеста
        // savings пополнений > 1000
        agg.put("SAVING", new TxAgg(new BigDecimal("1001"), BigDecimal.ZERO, 1));

        when(repo.fetchAggregatesByProductType(user)).thenReturn(agg);

        var res = rule.evaluate(user);
        assertTrue(res.isPresent(), "рекомендация должна выдаваться");
        ProductRecommendationDto dto = res.get();
        assertEquals("Invest 500", dto.name());
        assertEquals("147f6a0f-3b91-413b-ab99-87f081d60d5a", dto.id());
        assertNotNull(dto.text());
    }

    @Test
    void shouldNotRecommendWhenInvestAlreadyUsed() {
        UUID user = UUID.randomUUID();
        Map<String, TxAgg> agg = new HashMap<>();
        agg.put("DEBIT", new TxAgg(new BigDecimal("10"), BigDecimal.ZERO, 1));
        agg.put("SAVING", new TxAgg(new BigDecimal("5000"), BigDecimal.ZERO, 1));
        agg.put("INVEST", new TxAgg(BigDecimal.ZERO, BigDecimal.ZERO, 1));

        when(repo.fetchAggregatesByProductType(user)).thenReturn(agg);

        assertTrue(rule.evaluate(user).isEmpty(), "если INVEST уже есть — рекомендации быть не должно");
    }

    @Test
    void shouldNotRecommendWhenSavingDepositTooSmall() {
        UUID user = UUID.randomUUID();
        Map<String, TxAgg> agg = new HashMap<>();
        agg.put("DEBIT", new TxAgg(new BigDecimal("10"), BigDecimal.ZERO, 1));
        agg.put("SAVING", new TxAgg(new BigDecimal("999"), BigDecimal.ZERO, 1)); // < 1000

        when(repo.fetchAggregatesByProductType(user)).thenReturn(agg);

        assertTrue(rule.evaluate(user).isEmpty(), "если накопления < 1000 — не рекомендуем");
    }
}
