package com.starbank.reco.repo;


import java.math.BigDecimal;


public record TxAgg(BigDecimal depositSum, BigDecimal spendSum, long txCount) {
    public static final TxAgg EMPTY = new TxAgg(BigDecimal.ZERO, BigDecimal.ZERO, 0L);
}