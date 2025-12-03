package com.prueba.olap.domain.entity;

/**
 * Enumeración que define los tipos de consulta soportados.
 */
public enum QueryType {
    MDX("MDX"),
    SQL("SQL");
    
    private final String value;
    
    QueryType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static QueryType fromString(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Tipo no puede ser nulo");
        }
        
        for (QueryType queryType : values()) {
            if (queryType.value.equalsIgnoreCase(type)) {
                return queryType;
            }
        }
        
        throw new IllegalArgumentException("Tipo de consulta no soportado: " + type);
    }
}