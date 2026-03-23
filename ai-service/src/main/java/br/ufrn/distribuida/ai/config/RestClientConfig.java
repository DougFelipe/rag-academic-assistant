package br.ufrn.distribuida.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

/**
 * Configuração RestClient para Integração Spring AI Ollama.
 * 
 * <p>
 * Configura RestClient com suporte UTF-8 adequado e observabilidade
 * para chamadas à API do Ollama. Garante que caracteres portugueses em
 * prompts e respostas sejam tratados corretamente.
 * 
 * @author Equipe DISTRIBUIDA 3
 * @since Sprint 6 (UTF-8 Fix)
 * @since Sprint 7 (Observability/Tracing)
 */
@Configuration
public class RestClientConfig {

    /**
     * RestClient.Builder customizado para Ollama com UTF-8 e observabilidade.
     * 
     * <p>
     * <b>Recursos:</b>
     * <ul>
     * <li>Header Content-Type com UTF-8</li>
     * <li>Conversor Jackson customizado</li>
     * <li>Tratamento de charset para texto português</li>
     * <li>Observabilidade para tracing distribuído</li>
     * </ul>
     * 
     * @param objectMapper        ObjectMapper customizado com suporte UTF-8
     * @param observationRegistry Registry de observação para tracing
     * @return RestClient.Builder configurado
     */
    @Bean
    public RestClient.Builder restClientBuilder(ObjectMapper objectMapper, ObservationRegistry observationRegistry) {
        // Cria conversor de mensagens customizado com ObjectMapper UTF-8
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        return RestClient.builder()
                // Define Content-Type UTF-8 como padrão
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json; charset=UTF-8")
                // Adiciona observabilidade para tracing distribuído
                .observationRegistry(observationRegistry)
                // Adiciona conversor customizado
                .messageConverters(converters -> {
                    // Remove conversor padrão
                    converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
                    // Adiciona nosso conversor UTF-8
                    converters.add(converter);
                });
    }
}
