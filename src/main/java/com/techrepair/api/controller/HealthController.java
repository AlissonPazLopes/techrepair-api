package com.techrepair.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Monitoramento de disponibilidade e status da API")
public class HealthController {

    @GetMapping
    @Operation(
            summary = "Verificar status da API",
            description = "Endpoint público para verificar se a aplicação está no ar e respondendo normalmente."
    )
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", "techrepair-api");
        response.put("description", "API REST para Gestão de Assistência Técnica");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}
