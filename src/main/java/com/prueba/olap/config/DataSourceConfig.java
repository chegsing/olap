package com.prueba.olap.config;

import com.prueba.olap.adapter.JdbcOlapAdapter;
import com.prueba.olap.port.OlapQueryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource olapDataSource(Environment env) {
        String url = env.getProperty("app.datasource.url");
        String username = env.getProperty("app.datasource.username");
        String password = env.getProperty("app.datasource.password");
        String driver = env.getProperty("app.datasource.driver-class-name");

        var ds = new DriverManagerDataSource();
        if (driver != null && !driver.trim().isEmpty()) {
            ds.setDriverClassName(driver);
        }
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    public OlapQueryPort olapQueryPort(JdbcTemplate jdbcTemplate, Environment env) {
        String cubeView = env.getProperty("app.datasource.name");
        if (cubeView == null || cubeView.trim().isEmpty()) {
            cubeView = "cube_view"; // fallback logical name
        }
        return new JdbcOlapAdapter(jdbcTemplate, cubeView);
    }
}