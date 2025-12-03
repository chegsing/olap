package com.prueba.olap.service;

import com.prueba.olap.port.OlapQueryPort;
import com.prueba.olap.service.dto.AggregationResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OlapService {

    private final OlapQueryPort port;
    private final Set<String> allowedColumns;

    public OlapService(OlapQueryPort port, Set<String> allowedColumns) {
        this.port = port;
        this.allowedColumns = allowedColumns;
    }

    public String buildQuery(String dimensions, String measures, String filters) {
        var dims = splitAndTrim(dimensions);
        var meas = splitAndTrim(measures);
        var filt = parseFilters(filters);

        validateColumns(dims);
        validateColumns(meas);

        var selectCols = new ArrayList<String>(dims);
        selectCols.addAll(meas.stream().map(m -> "SUM(" + m + ") AS " + m).collect(Collectors.toList()));

        var groupBy = String.join(", ", dims);
        var select = String.join(", ", selectCols);

        var where = filt.entrySet().stream()
                .map(e -> e.getKey() + " = '" + e.getValue() + "'")
                .collect(Collectors.joining(" AND "));

        var table = "cube_view";

        var query = new StringBuilder();
        query.append("SELECT ").append(select).append(" FROM ").append(table);
        if (!where.isEmpty()) query.append(" WHERE ").append(where);
        if (!groupBy.isEmpty()) query.append(" GROUP BY ").append(groupBy);
        return query.toString();
    }

    public AggregationResponse execute(String sql) {
        return port.query(sql);
    }

    private List<String> splitAndTrim(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private Map<String, String> parseFilters(String filters) {
        if (filters == null || filters.isBlank()) return Map.of();
        return Arrays.stream(filters.split("\\|"))
                .map(s -> s.split(":", 2))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(a -> a[0].trim(), a -> a[1].trim()));
    }

    private void validateColumns(List<String> cols) {
        if (cols.isEmpty() || allowedColumns == null || allowedColumns.isEmpty()) return;
        for (var c : cols) {
            if (!allowedColumns.contains(c)) throw new IllegalArgumentException("Unauthorized column: " + c);
        }
    }
}