package com.prueba.olap.service;

import com.prueba.olap.port.OlapQueryPort;
import com.prueba.olap.service.dto.AggregationResponse;
import com.prueba.olap.service.dto.AggregationRow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

class OlapServiceTest {

    @Test
    void buildQuery_happyPath_generatesExpectedSql() {
        var captured = new AtomicReference<String>();
        OlapQueryPort fake = sql -> {
            captured.set(sql);
            var row = new AggregationRow(Map.of("Region", "EMEA", "Sales", 1000));
            return new AggregationResponse(List.of(row));
        };

        var service = new OlapService(fake, Set.of("Region", "Product", "Sales", "Quantity"));
        var sql = service.buildQuery("Region", "Sales", "Year:2024|Region:EMEA");

        Assertions.assertTrue(sql.contains("SELECT Region, SUM(Sales) AS Sales"));
        Assertions.assertTrue(sql.contains("FROM cube_view"));
        Assertions.assertTrue(sql.contains("WHERE Year = '2024' AND Region = 'EMEA'"));

        var resp = service.execute(sql);
        Assertions.assertNotNull(resp);
        Assertions.assertEquals(1, resp.getRows().size());
        Assertions.assertEquals("EMEA", resp.getRows().get(0).getValues().get("Region"));
    }

    @Test
    void buildQuery_emptyFilters_returnsGroupAndSelect() {
        var captured = new AtomicReference<String>();
        OlapQueryPort fake = sql -> {
            captured.set(sql);
            var row = new AggregationRow(Map.of("Product", "P1", "Quantity", 10));
            return new AggregationResponse(List.of(row));
        };

        var service = new OlapService(fake, Set.of("Region", "Product", "Sales", "Quantity"));
        var sql = service.buildQuery("Product", "Quantity", null);

        Assertions.assertTrue(sql.contains("SELECT Product, SUM(Quantity) AS Quantity"));
        Assertions.assertFalse(sql.contains("WHERE"));

        var resp = service.execute(sql);
        Assertions.assertEquals(1, resp.getRows().size());
    }
}
