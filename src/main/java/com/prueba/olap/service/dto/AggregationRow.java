package com.prueba.olap.service.dto;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * DTO que representa una fila de datos agregados en una respuesta OLAP.
 * Encapsula los valores de columnas como un mapa clave-valor.
 */
public class AggregationRow {
    
    private Map<String, Object> values;

    public AggregationRow() {
        this.values = Map.of();
    }

    public AggregationRow(Map<String, Object> values) {
        this.values = values != null ? Map.copyOf(values) : Map.of();
    }

    public Map<String, Object> getValues() {
        return values != null ? Map.copyOf(values) : Map.of();
    }

    public void setValues(Map<String, Object> values) {
        this.values = values != null ? Map.copyOf(values) : Map.of();
    }
    
    public Optional<Object> getValue(String columnName) {
        if (columnName == null || values == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(values.get(columnName));
    }
    
    public boolean hasColumn(String columnName) {
        return values != null && values.containsKey(columnName);
    }
    
    public int getColumnCount() {
        return values != null ? values.size() : 0;
    }
    
    public boolean isEmpty() {
        return values == null || values.isEmpty();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AggregationRow that = (AggregationRow) obj;
        return Objects.equals(values, that.values);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
    
    @Override
    public String toString() {
        return "AggregationRow{" +
               "columnCount=" + getColumnCount() +
               ", values=" + values +
               "}";
    }
}
