package com.prueba.olap.adapter;

import com.prueba.olap.port.OlapQueryPort;
import com.prueba.olap.service.dto.AggregationResponse;
import com.prueba.olap.service.dto.AggregationRow;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JdbcOlapAdapter implements OlapQueryPort {

    private final JdbcTemplate jdbcTemplate;
    private final String cubeView;

    public JdbcOlapAdapter(JdbcTemplate jdbcTemplate, String cubeView) {
        this.jdbcTemplate = jdbcTemplate;
        this.cubeView = cubeView;
    }

    @Override
    public AggregationResponse query(String sql) {
        var finalSql = sql.replace("cube_view", cubeView);

        List<Map<String, Object>> results = jdbcTemplate.queryForList(finalSql);
        var rows = new ArrayList<AggregationRow>();
        for (var map : results) {
            rows.add(new AggregationRow(map));
        }
        return new AggregationResponse(rows);
    }
}