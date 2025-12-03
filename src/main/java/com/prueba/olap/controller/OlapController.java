package com.prueba.olap.controller;

import com.prueba.olap.port.OlapQueryPort;
import com.prueba.olap.service.OlapService;
import com.prueba.olap.service.dto.AggregationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador REST para operaciones OLAP.
 * Expone endpoints para consultas agregadas sobre cubos de datos.
 */
@RestController
@RequestMapping("/api/olap")
@Tag(name = "OLAP", description = "API para consultas OLAP y análisis de datos")
public class OlapController {

    private static final String ALLOWED_COLUMNS_PROPERTY = "app.allowed-columns";
    
    private final OlapService olapService;

    public OlapController(OlapQueryPort queryPort, Environment environment) {
        validateConstructorParameters(queryPort, environment);
        Set<String> allowedColumns = parseAllowedColumns(environment.getProperty(ALLOWED_COLUMNS_PROPERTY));
        this.olapService = new OlapService(queryPort, allowedColumns);
    }

    @Operation(
        summary = "Ejecutar consulta OLAP agregada",
        description = "Ejecuta una consulta OLAP con dimensiones, medidas y filtros especificados"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consulta ejecutada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros de consulta inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping(value = "/aggregate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AggregationResponse> aggregate(
            @Parameter(
                description = "Dimensiones para agrupar, separadas por comas", 
                example = "Region,Product"
            )
            @RequestParam(name = "dimensions", required = false) String dimensions,
            
            @Parameter(
                description = "Medidas a agregar, separadas por comas", 
                example = "Sales,Quantity"
            )
            @RequestParam(name = "measures", required = false) String measures,
            
            @Parameter(
                description = "Filtros en formato clave:valor separados por |", 
                example = "Year:2024|Region:EMEA"
            )
            @RequestParam(name = "filters", required = false) String filters
    ) {
        return Mono.fromCallable(() -> {
            validateQueryParameters(dimensions, measures);
            String query = olapService.buildQuery(dimensions, measures, filters);
            return olapService.execute(query);
        }).onErrorMap(IllegalArgumentException.class, 
            ex -> new IllegalArgumentException("Parámetros inválidos: " + ex.getMessage()))
          .onErrorMap(Exception.class, 
            ex -> new RuntimeException("Error procesando consulta OLAP: " + ex.getMessage()));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(IllegalArgumentException e) {
        Map<String, String> error = Map.of(
            "error", "Parámetros inválidos",
            "message", e.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeError(RuntimeException e) {
        Map<String, String> error = Map.of(
            "error", "Error interno del servidor",
            "message", e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    private void validateConstructorParameters(OlapQueryPort port, Environment env) {
        if (port == null) {
            throw new IllegalArgumentException("Puerto de consulta OLAP no puede ser nulo");
        }
        if (env == null) {
            throw new IllegalArgumentException("Environment no puede ser nulo");
        }
    }
    
    private void validateQueryParameters(String dimensions, String measures) {
        if ((dimensions == null || dimensions.trim().isEmpty()) && 
            (measures == null || measures.trim().isEmpty())) {
            throw new IllegalArgumentException("Debe especificar al menos dimensiones o medidas");
        }
    }

    private Set<String> parseAllowedColumns(String property) {
        if (property == null || property.isBlank()) {
            return Set.of();
        }
        
        return Arrays.stream(property.split(","))
                .map(String::trim)
                .filter(column -> !column.isEmpty())
                .collect(Collectors.toSet());
    }
}