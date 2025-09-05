package com.starbank.reco.repo;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class TransactionStatsRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public TransactionStatsRepository(@Qualifier("knowledgeJdbc") NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, TxAgg> fetchAggregatesByProductType(UUID userId) {
        final String sql = """
                SELECT UPPER(p."TYPE")                         AS product_type,
                       SUM(CASE WHEN t."TYPE" = 'DEPOSIT'  THEN t.AMOUNT ELSE 0 END) AS deposit_sum,
                       SUM(CASE WHEN t."TYPE" = 'WITHDRAW' THEN t.AMOUNT ELSE 0 END) AS spend_sum,
                       COUNT(*)                                   AS tx_count
                  FROM TRANSACTIONS t
                  JOIN PRODUCTS p ON p.ID = t.PRODUCT_ID
                 WHERE t.USER_ID = CAST(:userId AS UUID)
                 GROUP BY UPPER(p."TYPE")
                """;

        var params = new MapSqlParameterSource()
                .addValue("userId", userId.toString());

        var rows = jdbc.query(sql, params, (rs, i) -> new Row(
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

    @Cacheable(cacheNames = "userOf", key = "#userId.toString() + '|' + #productType")
    public boolean isUserOf(UUID userId, String productType) {
        final String sql = """
                SELECT COUNT(*) > 0 AS ok
                  FROM TRANSACTIONS t
                  JOIN PRODUCTS p ON p.ID = t.PRODUCT_ID
                 WHERE t.USER_ID = CAST(:userId AS UUID)
                   AND UPPER(p."TYPE") = :ptype
                """;

        var params = new MapSqlParameterSource()
                .addValue("userId", userId.toString())
                .addValue("ptype", productType.toUpperCase());

        return Boolean.TRUE.equals(jdbc.queryForObject(sql, params, Boolean.class));
    }

    @Cacheable(cacheNames = "activeUserOf", key = "#userId.toString() + '|' + #productType")
    public boolean isActiveUserOf(UUID userId, String productType) {
        final String sql = """
                SELECT COUNT(*) >= 5 AS ok
                  FROM TRANSACTIONS t
                  JOIN PRODUCTS p ON p.ID = t.PRODUCT_ID
                 WHERE t.USER_ID = CAST(:userId AS UUID)
                   AND UPPER(p."TYPE") = :ptype
                """;

        var params = new MapSqlParameterSource()
                .addValue("userId", userId.toString())
                .addValue("ptype", productType.toUpperCase());

        return Boolean.TRUE.equals(jdbc.queryForObject(sql, params, Boolean.class));
    }

    @Cacheable(cacheNames = "sumByType", key = "#userId.toString() + '|' + #productType + '|' + #txType")
    public BigDecimal sumByProductAndTx(UUID userId, String productType, String txType) {
        final String sql = """
                SELECT COALESCE(SUM(CASE WHEN t."TYPE" = :tx THEN t.AMOUNT ELSE 0 END), 0) AS s
                  FROM TRANSACTIONS t
                  JOIN PRODUCTS p ON p.ID = t.PRODUCT_ID
                 WHERE t.USER_ID = CAST(:userId AS UUID)
                   AND UPPER(p."TYPE") = :ptype
                """;

        var params = new MapSqlParameterSource()
                .addValue("userId", userId.toString())
                .addValue("ptype", productType.toUpperCase())
                .addValue("tx", txType.toUpperCase());

        return jdbc.queryForObject(sql, params, BigDecimal.class);
    }

    @Cacheable(cacheNames = "sumDw", key = "#userId.toString() + '|' + #productType")
    public Dw sumDepositWithdraw(UUID userId, String productType) {
        final String sql = """
                SELECT COALESCE(SUM(CASE WHEN t."TYPE" = 'DEPOSIT'  THEN t.AMOUNT ELSE 0 END), 0) AS dep,
                       COALESCE(SUM(CASE WHEN t."TYPE" = 'WITHDRAW' THEN t.AMOUNT ELSE 0 END), 0) AS wdr
                  FROM TRANSACTIONS t
                  JOIN PRODUCTS p ON p.ID = t.PRODUCT_ID
                 WHERE t.USER_ID = CAST(:userId AS UUID)
                   AND UPPER(p."TYPE") = :ptype
                """;

        var params = new MapSqlParameterSource()
                .addValue("userId", userId.toString())
                .addValue("ptype", productType.toUpperCase());

        return jdbc.query(sql, params, rs ->
                rs.next()
                        ? new Dw(nz(rs.getBigDecimal("dep")), nz(rs.getBigDecimal("wdr")))
                        : new Dw(BigDecimal.ZERO, BigDecimal.ZERO)
        );
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private record Row(String productType, BigDecimal depositSum, BigDecimal spendSum, long txCount) {}
    public  record Dw(BigDecimal deposit, BigDecimal withdraw) {}
}