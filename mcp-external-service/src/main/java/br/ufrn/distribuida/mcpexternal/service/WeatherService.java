package br.ufrn.distribuida.mcpexternal.service;

import br.ufrn.distribuida.mcpexternal.client.OpenWeatherMapClient;
import br.ufrn.distribuida.mcpexternal.dto.WeatherRequest;
import br.ufrn.distribuida.mcpexternal.dto.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Serviço para operações meteorológicas usando OpenWeatherMap API.
 * 
 * <p>Integra com API externa via Feign Client para buscar dados de clima.
 */
@Service
@Slf4j
public class WeatherService {

    private final OpenWeatherMapClient weatherClient;

    @Value("${openweather.api.key}")
    private String apiKey;

    @Value("${openweather.api.default-city}")
    private String defaultCity;

    public WeatherService(OpenWeatherMapClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    /**
     * Obtém clima atual para uma cidade específica.
     *
     * @param city Nome da cidade (pode incluir código do país, ex: "Natal,BR")
     * @return Informações meteorológicas
     */
    public WeatherResponse getWeather(String city) {
        log.info("Fetching weather for city: {}", city);

        try {
            Map<String, Object> response = weatherClient.getCurrentWeather(
                city,
                apiKey,
                "metric",  // Celsius
                "pt_br"
            );

            return parseWeatherResponse(response, city);

        } catch (Exception e) {
            log.error("Error fetching weather for city {}: {}", city, e.getMessage());
            throw new RuntimeException("Failed to fetch weather data: " + e.getMessage(), e);
        }
    }

    /**
     * Obtém clima para cidade padrão (Natal, BR).
     * 
     * @return Informações meteorológicas de Natal
     */
    public WeatherResponse getDefaultWeather() {
        log.info("Fetching weather for default city: {}", defaultCity);
        return getWeather(defaultCity);
    }

    /**
     * Obtém clima usando objeto de request.
     * 
     * @param request Request com parâmetros detalhados
     * @return Informações meteorológicas
     */
    public WeatherResponse getWeather(WeatherRequest request) {
        String cityQuery = request.getCity();
        if (request.getCountry() != null && !request.getCountry().isEmpty()) {
            cityQuery = request.getCity() + "," + request.getCountry();
        }

        log.info("Fetching weather with request: city={}, units={}, lang={}",
            cityQuery, request.getUnits(), request.getLang());

        try {
            Map<String, Object> response = weatherClient.getCurrentWeather(
                cityQuery,
                apiKey,
                request.getUnits(),
                request.getLang()
            );

            return parseWeatherResponse(response, request.getCity());

        } catch (Exception e) {
            log.error("Error fetching weather for request {}: {}", request, e.getMessage());
            throw new RuntimeException("Failed to fetch weather data: " + e.getMessage(), e);
        }
    }

    /**
     * Faz parse da resposta da API OpenWeatherMap para WeatherResponse DTO.
     * 
     * @param response Resposta da API (Map JSON)
     * @param cityName Nome da cidade
     * @return DTO com dados meteorológicos formatados
     */
    @SuppressWarnings("unchecked")
    private WeatherResponse parseWeatherResponse(Map<String, Object> response, String cityName) {
        try {
            // Extract main weather data
            Map<String, Object> main = (Map<String, Object>) response.get("main");
            List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
            Map<String, Object> weather = weatherList.get(0);
            Map<String, Object> wind = (Map<String, Object>) response.get("wind");
            Map<String, Object> sys = (Map<String, Object>) response.get("sys");

            // Build response
            return WeatherResponse.builder()
                .city(cityName)
                .country((String) sys.get("country"))
                .temperature(getDoubleValue(main.get("temp")))
                .feelsLike(getDoubleValue(main.get("feels_like")))
                .humidity((Integer) main.get("humidity"))
                .windSpeed(getDoubleValue(wind.get("speed")))
                .description((String) weather.get("description"))
                .icon((String) weather.get("icon"))
                .timestamp(System.currentTimeMillis())
                .source("OpenWeatherMap")
                .build();

        } catch (Exception e) {
            log.error("Error parsing weather response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse weather data", e);
        }
    }

    /**
     * Converte Object para Double com segurança (trata Integer e Double do JSON).
     * 
     * @param value Valor a converter
     * @return Valor como Double ou 0.0 se inválido
     */
    private Double getDoubleValue(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        }
        return 0.0;
    }

    /**
     * Verifica se API key está configurada.
     * 
     * @return true se configurada, false caso contrário
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_API_KEY_HERE");
    }
}
