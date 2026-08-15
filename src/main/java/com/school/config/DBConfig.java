package com.school.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConfig {
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(EnvLoader.get("DB_URL", "jdbc:mysql://localhost:3306/gds_portal"));
        config.setUsername(EnvLoader.get("DB_USER", "root"));
        String dbPass = EnvLoader.get("DB_PASS", "password");
        if ("password".equals(dbPass))
            throw new IllegalStateException("DB_PASS is not configured. Set DB_PASS in the .env file.");
        config.setPassword(dbPass);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(30000);

        dataSource = new HikariDataSource(config);
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}
