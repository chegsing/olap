package com.prueba.olap.service.dto;

import java.util.Map;

public class AggregationRow {
    private Map<String, Object> values;

    public AggregationRow() {}

    public AggregationRow(Map<String, Object> values) {
        this.values = values;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }
}
