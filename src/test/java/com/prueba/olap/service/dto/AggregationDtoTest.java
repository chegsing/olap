package com.prueba.olap.service.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class AggregationDtoTest {

    @Test
    void aggregationResponse_and_row_gettersAndSetters() {
        var row = new AggregationRow(Map.of("a", 1));
        var resp = new AggregationResponse(List.of(row));

        Assertions.assertEquals(1, resp.getRows().size());
        Assertions.assertEquals(1, resp.getRows().get(0).getValues().get("a"));

        resp.setRows(List.of());
        Assertions.assertTrue(resp.getRows().isEmpty());
    }
}
