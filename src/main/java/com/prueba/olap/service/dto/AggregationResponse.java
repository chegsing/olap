package com.prueba.olap.service.dto;

import java.util.List;

public class AggregationResponse {
    private List<AggregationRow> rows;

    public AggregationResponse() {}

    public AggregationResponse(List<AggregationRow> rows) {
        this.rows = rows;
    }

    public List<AggregationRow> getRows() {
        return rows;
    }

    public void setRows(List<AggregationRow> rows) {
        this.rows = rows;
    }
}
