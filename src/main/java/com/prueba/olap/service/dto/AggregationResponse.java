package com.prueba.olap.service.dto;

import java.util.List;
import java.util.Objects;

/**
 * DTO que encapsula la respuesta de una consulta OLAP.
 * Contiene una lista de filas con datos agregados.
 */
public class AggregationResponse {
    
    private List<AggregationRow> rows;

    public AggregationResponse() {
        this.rows = List.of();
    }

    public AggregationResponse(List<AggregationRow> rows) {
        this.rows = rows != null ? List.copyOf(rows) : List.of();
    }

    public List<AggregationRow> getRows() {
        return rows != null ? List.copyOf(rows) : List.of();
    }

    public void setRows(List<AggregationRow> rows) {
        this.rows = rows != null ? List.copyOf(rows) : List.of();
    }
    
    public int getRowCount() {
        return rows != null ? rows.size() : 0;
    }
    
    public boolean isEmpty() {
        return rows == null || rows.isEmpty();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AggregationResponse that = (AggregationResponse) obj;
        return Objects.equals(rows, that.rows);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(rows);
    }
    
    @Override
    public String toString() {
        return "AggregationResponse{" +
               "rowCount=" + getRowCount() +
               "}";
    }
}
