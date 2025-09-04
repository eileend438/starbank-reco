package com.starbank.reco.rules.dynamic;


import com.starbank.reco.repo.TransactionStatsRepository;
import com.starbank.reco.rules.dynamic.dto.QueryDto;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


public class DynamicRuleEngine {
    private final TransactionStatsRepository repo;


    public DynamicRuleEngine(TransactionStatsRepository repo) { this.repo = repo; }


    public boolean evaluate(UUID userId, List<QueryDto> queries) {
        for (QueryDto q : queries) {
            boolean res = switch (q.query()) {
                case "USER_OF" -> expectArgs(q,1) && repo.isUserOf(userId, q.arguments().get(0));
                case "ACTIVE_USER_OF" -> expectArgs(q,1) && repo.isActiveUserOf(userId, q.arguments().get(0));
                case "TRANSACTION_SUM_COMPARE" -> txnSumCompare(userId, q);
                case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW" -> depVsWdr(userId, q);
                default -> throw new IllegalArgumentException("Unknown query: " + q.query());
            };
            if (q.negate()) res = !res;
            if (!res) return false; // AND-цепочка
        }
        return true;
    }


    private boolean txnSumCompare(UUID userId, QueryDto q) {
        if (!expectArgs(q,4)) return false;
        String productType = q.arguments().get(0);
        String txType = q.arguments().get(1);
        String op = q.arguments().get(2);
        int C = Integer.parseInt(q.arguments().get(3));
        BigDecimal sum = repo.sumByProductAndTx(userId, productType, txType);
        int cmp = sum.compareTo(new BigDecimal(C));
        return compare(cmp, op);
    }


    private boolean depVsWdr(UUID userId, QueryDto q) {
        if (!expectArgs(q,2)) return false;
        String productType = q.arguments().get(0);
        String op = q.arguments().get(1);
        var dw = repo.sumDepositWithdraw(userId, productType);
        int cmp = dw.deposit().compareTo(dw.withdraw());
        return compare(cmp, op);
    }


    private boolean compare(int cmp, String op) {
        return switch (op) {
            case ">" -> cmp > 0;
            case "<" -> cmp < 0;
            case "=" -> cmp == 0;
            case ">=" -> cmp >= 0;
            case "<=" -> cmp <= 0;
            default -> throw new IllegalArgumentException("Bad operator: " + op);
        };
    }


    private boolean expectArgs(QueryDto q, int n) {
        if (q.arguments() == null || q.arguments().size() < n)
            throw new IllegalArgumentException("Query " + q.query() + " expects " + n + " args");
        return true;
    }
}