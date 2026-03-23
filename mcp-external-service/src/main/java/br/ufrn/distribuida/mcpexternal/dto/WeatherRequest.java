package br.ufrn.distribuida.mcpexternal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para informações meteorológicas.
 * 
 * <p>Usado para solicitar clima de uma cidade específica com parâmetros customizados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherRequest {

    @NotBlank(message = "City name is required")
    private String city;

    @Builder.Default
    private String country = "BR";

    @Builder.Default
    private String units = "metric";  // metric (Celsius), imperial (Fahrenheit), standard (Kelvin)

    @Builder.Default
    private String lang = "pt_br";
}
