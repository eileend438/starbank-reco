package com.starbank.reco.repo;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class TransactionStatsRepository {

    private static final List<String> SPEND_OPS = List.of(
            "WITHDRAW", "PAYMENT", "PURCHASE", "SPEND", "TRANSFER_OUT"
    );

    private final NamedParameterJdbcTemplate jdbc;

    public TransactionStatsRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    public Map<String, TxAgg> fetchAggregatesByProductType(UUID userId) {
        final String sql = """
    SELECT UPPER(p."TYPE") AS product_type,
           SUM(CASE WHEN t."TYPE" = 'DEPOSIT' THEN t.AMOUNT ELSE 0 END) AS deposit_sum,
           SUM(CASE WHEN t."TYPE" = 'WITHDRAW' THEN t.AMOUNT ELSE 0 END) AS spend_sum,
           COUNT(*) AS tx_count
    FROM TRANSACTIONS t
    JOIN PRODUCTS p ON p.ID = t.PRODUCT_ID
    WHERE t.USER_ID = CAST(:userId AS UUID)
    GROUP BY UPPER(p."TYPE")
    """;

        var params = new MapSqlParameterSource()
                .addValue("userId", userId.toString());

        List<Row> rows = jdbc.query(sql, params, (rs, i) -> new Row(
                rs.getString("product_type"),
                nz(rs.getBigDecimal("deposit_sum")),
                nz(rs.getBigDecimal("spend_sum")),
                rs.getLong("tx_count")
        ));

        return rows.stream().collect(Collectors.toMap(
                r -> r.productType,
                r -> new TxAgg(r.depositSum, r.spendSum, r.txCount)
        ));
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private record Row(String productType, BigDecimal depositSum, BigDecimal spendSum, long txCount) {}
}