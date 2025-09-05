package com.starbank.reco.repo;

import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionStatsRepositoryTest {

    static DataSource ds;
    static NamedParameterJdbcTemplate jdbc;
    static TransactionStatsRepository repo;

    static UUID USER = UUID.randomUUID();
    static UUID PROD_DEBIT = UUID.randomUUID();
    static UUID PROD_SAVING = UUID.randomUUID();

    @BeforeAll
    static void setupDb() {
        ds = new DriverManagerDataSource("jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new NamedParameterJdbcTemplate(ds);
        repo = new TransactionStatsRepository(jdbc);

        jdbc.getJdbcTemplate().execute("""
                CREATE TABLE PRODUCTS(
                    ID UUID PRIMARY KEY,
                    "TYPE" VARCHAR(50),
                    NAME VARCHAR(255)
                );
                CREATE TABLE TRANSACTIONS(
                    ID UUID PRIMARY KEY,
                    PRODUCT_ID UUID,
                    USER_ID UUID,
                    "TYPE" VARCHAR(50),
                    AMOUNT DECIMAL(19,2)
                );
                """);


        jdbc.getJdbcTemplate().update("INSERT INTO PRODUCTS(ID, \"TYPE\", NAME) VALUES('" + PROD_DEBIT + "', 'DEBIT', 'Deb Card')", ps -> {});
        jdbc.getJdbcTemplate().update("INSERT INTO PRODUCTS(ID, \"TYPE\", NAME) VALUES('" + PROD_SAVING + "', 'SAVING', 'Vault')", ps -> {});

        jdbc.getJdbcTemplate().update("INSERT INTO TRANSACTIONS VALUES(random_uuid(), '" + PROD_SAVING + "', '" + USER + "', 'DEPOSIT', 1000)", ps -> {});
        jdbc.getJdbcTemplate().update("INSERT INTO TRANSACTIONS VALUES(random_uuid(), '" + PROD_SAVING + "', '" + USER + "', 'DEPOSIT', 500)", ps -> {});
        jdbc.getJdbcTemplate().update("INSERT INTO TRANSACTIONS VALUES(random_uuid(), '" + PROD_DEBIT + "', '" + USER + "', 'WITHDRAW', 200)", ps -> {});
        jdbc.getJdbcTemplate().update("INSERT INTO TRANSACTIONS VALUES(random_uuid(), '" + PROD_DEBIT + "', '" + USER + "', 'DEPOSIT', 300)", ps -> {});
    }

    @Test
    void isUserOf_Works() {
        assertTrue(repo.isUserOf(USER, "DEBIT"));
        assertTrue(repo.isUserOf(USER, "SAVING"));
        assertFalse(repo.isUserOf(USER, "INVEST"));
    }

    @Test
    void isActiveUserOf_CountGte5() {
        // сейчас у DEBIT всего 2 операции — не активный
        assertFalse(repo.isActiveUserOf(USER, "DEBIT"));
    }

    @Test
    void sumByProductAndTx_Works() {
        BigDecimal s = repo.sumByProductAndTx(USER, "SAVING", "DEPOSIT");
        assertEquals(new BigDecimal("1500.00"), s);
    }

    @Test
    void sumDepositWithdraw_Works() {
        var dw = repo.sumDepositWithdraw(USER, "DEBIT");
        assertEquals(new BigDecimal("300.00"), dw.deposit());
        assertEquals(new BigDecimal("200.00"), dw.withdraw());
    }

    @Test
    void fetchAggregatesByProductType_Works() {
        Map<String, TxAgg> agg = repo.fetchAggregatesByProductType(USER);
        assertTrue(agg.containsKey("DEBIT"));
        assertTrue(agg.containsKey("SAVING"));
        assertEquals(new BigDecimal("300.00"), agg.get("DEBIT").depositSum());
        assertEquals(new BigDecimal("200.00"), agg.get("DEBIT").spendSum());
        assertEquals(new BigDecimal("1500.00"), agg.get("SAVING").depositSum());
    }
}
