package br.ufrn.distribuida.mcpexternal.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Feign Client para OpenWeatherMap API.
 *
 * <p>Integra com API externa para buscar dados meteorológicos.
 * Documentação: https://openweathermap.org/current
 */
@FeignClient(
    name = "openweathermap",
    url = "${openweather.api.base-url}"
)
public interface OpenWeatherMapClient {

    /**
     * Obtém clima atual para uma cidade.
     *
     * @param city Nome da cidade (ex: "Natal", "Natal,BR")
     * @param appid API key do OpenWeatherMap
     * @param units Unidades de medida (standard, metric, imperial)
     * @param lang Idioma para descrição do clima
     * @return Dados meteorológicos do OpenWeatherMap
     */
    @GetMapping("/weather")
    Map<String, Object> getCurrentWeather(
        @RequestParam("q") String city,
        @RequestParam("appid") String appid,
        @RequestParam("units") String units,
        @RequestParam("lang") String lang
    );
}
