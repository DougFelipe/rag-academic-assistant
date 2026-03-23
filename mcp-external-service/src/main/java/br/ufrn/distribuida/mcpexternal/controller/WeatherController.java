package br.ufrn.distribuida.mcpexternal.controller;

import br.ufrn.distribuida.mcpexternal.dto.WeatherRequest;
import br.ufrn.distribuida.mcpexternal.dto.WeatherResponse;
import br.ufrn.distribuida.mcpexternal.service.WeatherService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller para MCP External Service - API de Clima.
 *
 * <p>Fornece endpoints para buscar informações meteorológicas do OpenWeatherMap.
 * Estes dados podem ser usados pela IA para enriquecer respostas sobre clima.
 */
@RestController
@RequestMapping("/api/mcp-external")
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * Obtém clima para uma cidade específica.
     *
     * @param city Nome da cidade (pode incluir código do país, ex: "Natal,BR")
     * @return Informações meteorológicas
     */
    @GetMapping("/weather/{city}")
    public ResponseEntity<WeatherResponse> getWeather(@PathVariable String city) {
        log.info("GET /api/mcp-external/weather/{}", city);

        try {
            WeatherResponse weather = weatherService.getWeather(city);
            return ResponseEntity.ok(weather);

        } catch (Exception e) {
            log.error("Error getting weather for city {}: {}", city, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtém clima para cidade padrão (Natal, BR).
     *
     * @return Informações meteorológicas de Natal
     */
    @GetMapping("/weather")
    public ResponseEntity<WeatherResponse> getDefaultWeather() {
        log.info("GET /api/mcp-external/weather - Getting default city weather");

        try {
            WeatherResponse weather = weatherService.getDefaultWeather();
            return ResponseEntity.ok(weather);

        } catch (Exception e) {
            log.error("Error getting default weather: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtém clima usando POST com parâmetros detalhados.
     *
     * @param request Request com cidade, país, unidades, idioma
     * @return Informações meteorológicas
     */
    @PostMapping("/weather")
    public ResponseEntity<WeatherResponse> getWeatherWithRequest(
            @RequestBody @Valid WeatherRequest request) {

        log.info("POST /api/mcp-external/weather - Request: {}", request);

        try {
            WeatherResponse weather = weatherService.getWeather(request);
            return ResponseEntity.ok(weather);

        } catch (Exception e) {
            log.error("Error getting weather for request {}: {}", request, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de health check.
     *
     * @return Status de saúde do serviço
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean configured = weatherService.isConfigured();

        Map<String, Object> health = Map.of(
            "status", configured ? "UP" : "DEGRADED",
            "service", "MCP External Service",
            "api", "OpenWeatherMap",
            "configured", configured,
            "message", configured
                ? "Service is healthy and API key is configured"
                : "Service is running but API key is not configured (using placeholder)"
        );

        log.debug("GET /api/mcp-external/health - {}", health);

        return configured
            ? ResponseEntity.ok(health)
            : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
    }

    /**
     * Obtém informações do serviço.
     *
     * @return Metadados do serviço
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = Map.of(
            "name", "MCP External Service",
            "description", "Third-party MCP Server integration (OpenWeatherMap API)",
            "version", "1.0.0",
            "provider", "OpenWeatherMap",
            "endpoints", Map.of(
                "GET /api/mcp-external/weather/{city}", "Get weather for specific city",
                "GET /api/mcp-external/weather", "Get weather for default city (Natal)",
                "POST /api/mcp-external/weather", "Get weather with detailed request",
                "GET /api/mcp-external/health", "Health check",
                "GET /api/mcp-external/info", "Service information"
            )
        );

        log.debug("GET /api/mcp-external/info");
        return ResponseEntity.ok(info);
    }
}
