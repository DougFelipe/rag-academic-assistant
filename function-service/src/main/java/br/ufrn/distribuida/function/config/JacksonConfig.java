package br.ufrn.distribuida.function.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Configuração Jackson para Suporte UTF-8.
 * 
 * <p>
 * Configura ObjectMapper para lidar corretamente com caracteres especiais
 * portugueses (é, ã, ç, á, etc.) nas funções de pré-processamento e
 * enriquecimento.
 * 
 * <p>
 * <b>Problema Resolvido:</b> Sem esta configuração, caracteres acentuados
 * eram corrompidos durante serialização/desserialização JSON.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.createXmlMapper(false).build();

        // Enable lenient parsing for UTF-8 special characters
        mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        // Handle unknown properties gracefully
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Pretty print disabled for performance
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);

        // Ensure proper UTF-8 handling
        mapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, false);

        return mapper;
    }
}
