package com.prueba.olap.service;

import com.prueba.olap.port.OlapQueryPort;
import com.prueba.olap.service.dto.AggregationResponse;
import com.prueba.olap.service.dto.AggregationRow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

class OlapServiceEdgeTest {

    @Test
    void buildQuery_unauthorizedColumn_throws() {
        OlapQueryPort fake = (sql, params) -> new AggregationResponse(List.of());
        var service = new OlapService(fake, Set.of("Allowed"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> service.buildQuery("NotAllowed", "Sales", null));
    }

    @Test
    void buildQuery_onlyMeasures_noGroupBy() {
        OlapQueryPort fake = (sql, params) -> new AggregationResponse(List.of(new AggregationRow(Map.of("Sales", 10))));
        var service = new OlapService(fake, Set.of("Sales"));
        var sql = service.buildQuery(null, "Sales", null);
        Assertions.assertTrue(sql.contains("SUM(Sales) AS Sales"));
        Assertions.assertFalse(sql.contains("GROUP BY"));
    }

    @Test
    void parseFilters_handlesEmptyAndMalformed() {
        OlapQueryPort fake = (sql, params) -> new AggregationResponse(List.of());
        var service = new OlapService(fake, Set.of());
        var q1 = service.buildQuery(null, null, null);
        Assertions.assertNotNull(q1);

        var q2 = service.buildQuery(null, null, "badfilter");
        Assertions.assertNotNull(q2);
    }
}
