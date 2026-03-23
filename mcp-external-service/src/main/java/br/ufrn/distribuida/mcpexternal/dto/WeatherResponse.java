package br.ufrn.distribuida.mcpexternal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta com informações meteorológicas.
 * 
 * <p>Contém dados de clima para uma cidade.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {

    private String city;

    private String country;

    private Double temperature;

    private Double feelsLike;

    private Integer humidity;

    private Double windSpeed;

    private String description;

    private String icon;

    private Long timestamp;

    private String source;

    /**
     * Obtém sumário legível do clima.
     * 
     * @return Descrição formatada do clima
     */
    public String getSummary() {
        return String.format(
            "Clima em %s, %s: %s. Temperatura: %.1f°C (sensacao termica: %.1f°C). Umidade: %d%%. Vento: %.1f m/s.",
            city,
            country,
            description,
            temperature,
            feelsLike,
            humidity,
            windSpeed
        );
    }
}
