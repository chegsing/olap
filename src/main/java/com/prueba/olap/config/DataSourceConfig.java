package com.prueba.olap.config;

import com.prueba.olap.adapter.JdbcOlapAdapter;
import com.prueba.olap.port.OlapQueryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Configuración de fuentes de datos y adaptadores OLAP.
 * Implementa el patrón de configuración de Spring para inyección de dependencias.
 */
@Configuration
public class DataSourceConfig {

    private static final String DEFAULT_CUBE_VIEW = "vw_olap_cube";
    private static final String URL_PROPERTY = "app.datasource.url";
    private static final String USERNAME_PROPERTY = "app.datasource.username";
    private static final String PASSWORD_PROPERTY = "app.datasource.password";
    private static final String DRIVER_PROPERTY = "app.datasource.driver-class-name";
    private static final String CUBE_VIEW_PROPERTY = "app.datasource.name";

    @Bean
    public DataSource olapDataSource(Environment environment) {
        validateEnvironment(environment);
        
        String url = getRequiredProperty(environment, URL_PROPERTY);
        String username = getRequiredProperty(environment, USERNAME_PROPERTY);
        String password = getRequiredProperty(environment, PASSWORD_PROPERTY);
        String driver = environment.getProperty(DRIVER_PROPERTY);

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        
        if (driver != null && !driver.trim().isEmpty()) {
            dataSource.setDriverClassName(driver);
        }
        
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        
        return dataSource;
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        validateDataSource(dataSource);
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public OlapQueryPort olapQueryPort(NamedParameterJdbcTemplate jdbcTemplate, Environment environment) {
        validateJdbcTemplate(jdbcTemplate);
        validateEnvironment(environment);
        
        String cubeViewName = getCubeViewName(environment);
        return new JdbcOlapAdapter(jdbcTemplate, cubeViewName);
    }
    
    private void validateEnvironment(Environment environment) {
        if (environment == null) {
            throw new IllegalArgumentException("Environment no puede ser nulo");
        }
    }
    
    private void validateDataSource(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource no puede ser nulo");
        }
    }
    
    private void validateJdbcTemplate(NamedParameterJdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new IllegalArgumentException("JdbcTemplate no puede ser nulo");
        }
    }
    
    private String getRequiredProperty(Environment environment, String propertyName) {
        String value = environment.getProperty(propertyName);
        if (value == null) {
            throw new IllegalArgumentException(
                "Propiedad requerida no encontrada: " + propertyName);
        }
        return value;
    }
    
    private String getCubeViewName(Environment environment) {
        String cubeView = environment.getProperty(CUBE_VIEW_PROPERTY);
        
        if (cubeView == null || cubeView.trim().isEmpty()) {
            return DEFAULT_CUBE_VIEW;
        }
        
        return cubeView.trim();
    }
}