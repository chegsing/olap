package com.prueba.olap.port;

import com.prueba.olap.service.dto.AggregationResponse;

import java.util.Map;

/**
 * Puerto para consultas OLAP que soporta tanto MDX como SQL.
 * Implementa el patrón Port-Adapter de arquitectura hexagonal.
 */
public interface OlapQueryPort {
    
    /**
     * Ejecuta una consulta OLAP con parámetros opcionales.
     * 
     * @param query La consulta MDX o SQL a ejecutar
     * @param params Parámetros nombrados para la consulta (puede ser null o vacío)
     * @return Respuesta con los datos agregados
     */
    AggregationResponse query(String query, Map<String, Object> params);
    
    /**
     * Ejecuta una consulta OLAP sin parámetros.
     * 
     * @param query La consulta MDX o SQL a ejecutar
     * @return Respuesta con los datos agregados
     */
    default AggregationResponse query(String query) {
        return query(query, Map.of());
    }
}