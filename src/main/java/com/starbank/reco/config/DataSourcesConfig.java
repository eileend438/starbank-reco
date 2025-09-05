package com.starbank.reco.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.time.Duration;

@Configuration
@EnableCaching
public class DataSourcesConfig {


    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties defaultDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Primary
    @Bean(name = "defaultDataSource")
    public DataSource defaultDataSource(
            @Qualifier("defaultDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }


    @Bean
    @ConfigurationProperties("knowledge.datasource")
    public DataSourceProperties knowledgeDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Bean(name = "knowledgeDataSource")
    public DataSource knowledgeDataSource(
            @Qualifier("knowledgeDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder().build();
    }


    @Bean(name = "knowledgeJdbc")
    public NamedParameterJdbcTemplate knowledgeJdbc(
            @Qualifier("knowledgeDataSource") DataSource knowledgeDataSource) {
        return new NamedParameterJdbcTemplate(knowledgeDataSource);
    }


    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager m = new CaffeineCacheManager("userOf", "activeUserOf", "sumByType", "sumDw");
        m.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(10)));
        return m;
    }
}
