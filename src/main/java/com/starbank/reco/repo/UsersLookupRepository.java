package com.starbank.reco.repo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UsersLookupRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public record UserRow(UUID id, String name) {}

    public UsersLookupRepository(@Qualifier("knowledgeJdbc") NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<UserRow> findByExactName(String name) {
        String sql = "SELECT ID, NAME FROM USERS WHERE NAME = :name";
        return jdbc.query(sql, Map.of("name", name),
                (rs,i) -> new UserRow(UUID.fromString(rs.getString("ID")), rs.getString("NAME")));
    }
}
