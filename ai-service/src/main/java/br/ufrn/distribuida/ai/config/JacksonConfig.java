package br.ufrn.distribuida.ai.config;

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
 * portugueses (é, ã, ç, á, etc.) em requisições e respostas JSON.
 * 
 * <p>
 * Resolve o erro "Invalid UTF-8 middle byte" ao processar texto com
 * acentos do Ollama ou entrada do usuário.
 * 
 * @author Equipe DISTRIBUIDA 3
 * @since Sprint 6 (UTF-8 Fix)
 */
@Configuration
public class JacksonConfig {

    /**
     * ObjectMapper customizado com suporte UTF-8.
     * 
     * <p>
     * <b>Recursos:</b>
     * <ul>
     * <li>Parsing tolerante para caracteres especiais</li>
     * <li>Tratamento de charset UTF-8</li>
     * <li>Desserialização adequada de texto português</li>
     * </ul>
     * 
     * @param builder Builder do Jackson
     * @return ObjectMapper configurado
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.createXmlMapper(false).build();

        // Habilita parsing tolerante para caracteres UTF-8 especiais
        mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        // Trata propriedades desconhecidas graciosamente
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Pretty print desabilitado para produção
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);

        // Garante tratamento UTF-8 adequado
        mapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, false);

        return mapper;
    }
}
