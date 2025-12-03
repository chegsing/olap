package com.prueba.olap.service;

import com.prueba.olap.port.OlapQueryPort;
import com.prueba.olap.service.dto.AggregationResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para operaciones OLAP.
 * Implementa casos de uso y orquesta la lógica de negocio.
 */
public class OlapService {

    private static final String CUBE_VIEW_PLACEHOLDER = "cube_view";
    private static final String FILTER_SEPARATOR = "\\|";
    private static final String KEY_VALUE_SEPARATOR = ":";
    private static final int MAX_PARAMETERS = 3;
    
    private final OlapQueryPort queryPort;
    private final Set<String> allowedColumns;

    public OlapService(OlapQueryPort queryPort, Set<String> allowedColumns) {
        validateConstructorParameters(queryPort, allowedColumns);
        this.queryPort = queryPort;
        this.allowedColumns = Set.copyOf(allowedColumns);
    }

    public String buildQuery(String dimensions, String measures, String filters) {
        List<String> dimensionList = parseAndValidateColumns(dimensions);
        List<String> measureList = parseAndValidateColumns(measures);
        Map<String, String> filterMap = parseFilters(filters);

        return constructSqlQuery(dimensionList, measureList, filterMap);
    }

    public AggregationResponse execute(String sql) {
        validateSqlQuery(sql);
        return queryPort.query(sql, Map.of());
    }
    
    private void validateConstructorParameters(OlapQueryPort port, Set<String> columns) {
        if (port == null) {
            throw new IllegalArgumentException("Puerto de consulta no puede ser nulo");
        }
        if (columns == null) {
            throw new IllegalArgumentException("Columnas permitidas no pueden ser nulas");
        }
    }
    
    private List<String> parseAndValidateColumns(String input) {
        List<String> columns = splitAndTrim(input);
        validateColumns(columns);
        return columns;
    }
    
    private String constructSqlQuery(List<String> dimensions, List<String> measures, Map<String, String> filters) {
        List<String> selectColumns = buildSelectColumns(dimensions, measures);
        String selectClause = String.join(", ", selectColumns);
        String whereClause = buildWhereClause(filters);
        String groupByClause = buildGroupByClause(dimensions);
        
        StringBuilder query = new StringBuilder();
        query.append("SELECT ").append(selectClause)
             .append(" FROM ").append(CUBE_VIEW_PLACEHOLDER);
        
        if (!whereClause.isEmpty()) {
            query.append(" WHERE ").append(whereClause);
        }
        
        if (!groupByClause.isEmpty()) {
            query.append(" GROUP BY ").append(groupByClause);
        }
        
        return query.toString();
    }
    
    private List<String> buildSelectColumns(List<String> dimensions, List<String> measures) {
        List<String> selectColumns = new ArrayList<>(dimensions);
        List<String> aggregatedMeasures = measures.stream()
            .map(measure -> "SUM(" + measure + ") AS " + measure)
            .collect(Collectors.toList());
        selectColumns.addAll(aggregatedMeasures);
        return selectColumns;
    }
    
    private String buildWhereClause(Map<String, String> filters) {
        return filters.entrySet().stream()
            .map(entry -> entry.getKey() + " = '" + sanitizeValue(entry.getValue()) + "'")
            .collect(Collectors.joining(" AND "));
    }
    
    private String buildGroupByClause(List<String> dimensions) {
        return String.join(", ", dimensions);
    }
    
    private String sanitizeValue(String value) {
        return value.replace("'", "''"); // Escape single quotes
    }
    
    private void validateSqlQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("Consulta SQL no puede ser nula o vacía");
        }
    }

    private List<String> splitAndTrim(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private Map<String, String> parseFilters(String filters) {
        if (filters == null || filters.isBlank()) {
            return Map.of();
        }
        
        Map<String, String> filterMap = Arrays.stream(filters.split(FILTER_SEPARATOR))
                .map(s -> s.split(KEY_VALUE_SEPARATOR, 2))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(
                    arr -> arr[0].trim(), 
                    arr -> arr[1].trim()
                ));
        
        validateFilterCount(filterMap);
        return filterMap;
    }
    
    private void validateFilterCount(Map<String, String> filters) {
        if (filters.size() > MAX_PARAMETERS) {
            throw new IllegalArgumentException(
                "Máximo " + MAX_PARAMETERS + " filtros permitidos, recibidos: " + filters.size());
        }
    }

    private void validateColumns(List<String> columns) {
        if (columns.isEmpty() || allowedColumns.isEmpty()) {
            return;
        }
        
        for (String column : columns) {
            if (!allowedColumns.contains(column)) {
                throw new IllegalArgumentException("Columna no autorizada: " + column);
            }
        }
    }
}