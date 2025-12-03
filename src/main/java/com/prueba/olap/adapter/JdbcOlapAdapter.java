package com.prueba.olap.adapter;

import com.prueba.olap.port.OlapQueryPort;
import com.prueba.olap.service.dto.AggregationResponse;
import com.prueba.olap.service.dto.AggregationRow;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador JDBC para consultas OLAP contra bases de datos relacionales.
 * Implementa el patrón Adapter de arquitectura hexagonal.
 */
public class JdbcOlapAdapter implements OlapQueryPort {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final String cubeViewName;

    public JdbcOlapAdapter(NamedParameterJdbcTemplate jdbcTemplate, String cubeViewName) {
        validateParameters(jdbcTemplate, cubeViewName);
        this.jdbcTemplate = jdbcTemplate;
        this.cubeViewName = cubeViewName;
    }

    @Override
    public AggregationResponse query(String sql, Map<String, Object> params) {
        validateQuery(sql);
        
        try {
            String finalSql = replaceCubeViewPlaceholder(sql);
            Map<String, Object> safeParams = params != null ? params : Map.of();
            
            SqlRowSet resultSet = jdbcTemplate.queryForRowSet(finalSql, 
                new MapSqlParameterSource(safeParams));
            
            List<AggregationRow> rows = processResultSet(resultSet);
            return new AggregationResponse(rows);
            
        } catch (DataAccessException e) {
            throw new RuntimeException("Error ejecutando consulta SQL: " + e.getMessage(), e);
        }
    }
    
    private void validateParameters(NamedParameterJdbcTemplate jdbcTemplate, String cubeViewName) {
        if (jdbcTemplate == null) {
            throw new IllegalArgumentException("JdbcTemplate no puede ser nulo");
        }
        if (cubeViewName == null || cubeViewName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre de vista del cubo no puede ser nulo o vacío");
        }
    }
    
    private void validateQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("La consulta SQL no puede ser nula o vacía");
        }
    }
    
    private String replaceCubeViewPlaceholder(String sql) {
        return sql.replace("cube_view", cubeViewName);
    }
    
    private List<AggregationRow> processResultSet(SqlRowSet resultSet) {
        List<AggregationRow> rows = new ArrayList<>();
        
        while (resultSet.next()) {
            Map<String, Object> rowData = extractRowData(resultSet);
            rows.add(new AggregationRow(rowData));
        }
        
        return rows;
    }
    
    private Map<String, Object> extractRowData(SqlRowSet resultSet) {
        SqlRowSetMetaData metaData = resultSet.getMetaData();
        Map<String, Object> rowData = new HashMap<>();
        
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnLabel = metaData.getColumnLabel(i);
            Object columnValue = resultSet.getObject(i);
            rowData.put(columnLabel, columnValue);
        }
        
        return rowData;
    }
}