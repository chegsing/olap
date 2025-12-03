package com.prueba.olap.domain.entity;

import java.util.Map;
import java.util.Objects;

/**
 * Entidad de dominio que representa una consulta OLAP.
 * Libre de dependencias externas siguiendo principios de Clean Architecture.
 */
public class Query {
    
    private final String statement;
    private final QueryType type;
    private final Map<String, Object> parameters;
    
    public Query(String statement, QueryType type, Map<String, Object> parameters) {
        validateStatement(statement);
        validateType(type);
        
        this.statement = statement;
        this.type = type;
        this.parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
    }
    
    public String getStatement() {
        return statement;
    }
    
    public QueryType getType() {
        return type;
    }
    
    public Map<String, Object> getParameters() {
        return Map.copyOf(parameters);
    }
    
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }
    
    private void validateStatement(String statement) {
        if (statement == null || statement.trim().isEmpty()) {
            throw new IllegalArgumentException("Statement no puede ser nulo o vacío");
        }
    }
    
    private void validateType(QueryType type) {
        if (type == null) {
            throw new IllegalArgumentException("Tipo de consulta no puede ser nulo");
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Query query = (Query) obj;
        return Objects.equals(statement, query.statement) &&
               type == query.type &&
               Objects.equals(parameters, query.parameters);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(statement, type, parameters);
    }
    
    @Override
    public String toString() {
        return "Query{" +
               "type=" + type +
               ", statement='" + statement + '\'' +
               ", parametersCount=" + parameters.size() +
               '}';
    }
}