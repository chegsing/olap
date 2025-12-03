package com.prueba.olap.controller;

import com.prueba.olap.port.OlapQueryPort;
import com.prueba.olap.service.OlapService;
import com.prueba.olap.service.dto.AggregationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/olap")
public class OlapController {

    private final OlapService service;

    public OlapController(OlapQueryPort port, Environment env) {
        var allowed = parseAllowed(env.getProperty("app.allowed-columns"));
        this.service = new OlapService(port, allowed);
    }

    @Operation(summary = "Consulta OLAP agregada",
            description = "Recibe parámetros de consulta y devuelve agregados desde un cubo OLAP en SQL Server.")
    @GetMapping(value = "/aggregate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AggregationResponse> aggregate(
            @Parameter(description = "Dimensiones a agregar, separadas por comas", example = "Region,Product")
            @RequestParam(name = "dimensions", required = false) String dimensions,
            @Parameter(description = "Medidas a solicitar, separadas por comas", example = "Sales,Quantity")
            @RequestParam(name = "measures", required = false) String measures,
            @Parameter(description = "Filtros simples en formato key:value|key2:value2", example = "Year:2024|Region:EMEA")
            @RequestParam(name = "filters", required = false) String filters
    ) {
        var query = service.buildQuery(dimensions, measures, filters);
        return Mono.fromCallable(() -> service.execute(query));
    }

    private Set<String> parseAllowed(String prop) {
        if (prop == null || prop.isBlank()) return Set.of();
        return Arrays.stream(prop.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}