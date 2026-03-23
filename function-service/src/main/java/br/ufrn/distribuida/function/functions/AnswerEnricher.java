package br.ufrn.distribuida.function.functions;

import br.ufrn.distribuida.function.dto.AnswerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Function for enriching AI-generated answers.
 * 
 * Responsibilities:
 * 1. Add metadata (timestamp, version, source)
 * 2. Format answer text (trim, clean)
 * 3. Add disclaimer if confidence is low (< 0.5)
 * 
 * This function is exposed as a Spring Cloud Function and can be called via
 * HTTP:
 * POST /enrichAnswer
 * 
 * @since Sprint 7
 */
@Component("enrichAnswer")
@Slf4j
public class AnswerEnricher implements Function<AnswerDTO, AnswerDTO> {

    private static final String SERVICE_NAME = "IMD Professor Assistant";
    private static final String VERSION = "1.0.0";
    private static final double LOW_CONFIDENCE_THRESHOLD = 0.5;

    private static final String DISCLAIMER_TEXT = "\n\n *Nota: Esta informação tem baixa confiança. " +
            "Por favor, verifique com o professor ou a secretaria do IMD.*";

    @Override
    public AnswerDTO apply(AnswerDTO input) {
        log.info("Enriching answer - Confidence: {}", input.getConfidence());

        long startTime = System.currentTimeMillis();

        // 1. Enrich metadata
        Map<String, Object> enrichedMetadata = enrichMetadata(input);

        // 2. Format answer
        String formattedAnswer = formatAnswer(input.getAnswer());

        // 3. Add disclaimer if needed
        String finalAnswer = addDisclaimerIfNeeded(formattedAnswer, input.getConfidence());

        // 4. Build enriched result
        AnswerDTO result = AnswerDTO.builder()
                .answer(finalAnswer)
                .sources(input.getSources())
                .confidence(input.getConfidence())
                .metadata(enrichedMetadata)
                .build();

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("Answer enriched - Added disclaimer: {}, Time: {}ms",
                finalAnswer.contains("Nota:"), processingTime);

        return result;
    }

    /**
     * Enriquece metadados com timestamp, versão e informações de fonte.
     * 
     * @param input DTO da resposta original
     * @return Mapa de metadados enriquecido
     */
    private Map<String, Object> enrichMetadata(AnswerDTO input) {
        Map<String, Object> metadata = new HashMap<>(input.getMetadata());

        // Add enrichment timestamp
        metadata.put("enriched_at", Instant.now().toString());
        metadata.put("enrichment_timestamp", System.currentTimeMillis());

        // Add version and source
        metadata.put("service_version", VERSION);
        metadata.put("service_name", SERVICE_NAME);

        // Add answer statistics
        metadata.put("answer_length", input.getAnswer() != null ? input.getAnswer().length() : 0);
        metadata.put("source_count", input.getSources() != null ? input.getSources().size() : 0);
        metadata.put("has_disclaimer",
                input.getConfidence() != null && input.getConfidence() < LOW_CONFIDENCE_THRESHOLD);

        log.debug("Metadata enriched with {} entries", metadata.size());
        return metadata;
    }

    /**
     * Formata texto da resposta fazendo trim e limpeza.
     * 
     * <p>
     * <b>Melhorias futuras podem incluir:</b>
     * <ul>
     * <li>Formatação Markdown</li>
     * <li>Normalização de quebras de linha</li>
     * <li>Formatação de bullet points</li>
     * </ul>
     * 
     * @param answer Texto da resposta original
     * @return Resposta formatada
     */
    private String formatAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            log.warn("Empty answer received for formatting");
            return "";
        }

        // Trim whitespace
        String formatted = answer.trim();

        // Replace multiple newlines with double newline
        formatted = formatted.replaceAll("\n{3,}", "\n\n");

        // Replace multiple spaces with single space
        formatted = formatted.replaceAll(" {2,}", " ");

        log.debug("Answer formatted - Original length: {}, Formatted length: {}",
                answer.length(), formatted.length());

        return formatted;
    }

    /**
     * Add disclaimer to answer if confidence is below threshold.
     * 
     * Low confidence (< 0.5) indicates that the AI is not very certain
     * about the answer, so we add a warning for the user to verify.
     * 
     * @param answer     The formatted answer
     * @param confidence The confidence score (0.0 to 1.0)
     * @return Answer with or without disclaimer
     */
    private String addDisclaimerIfNeeded(String answer, Double confidence) {
        if (confidence == null) {
            log.warn("No confidence score provided, adding disclaimer by default");
            return answer + DISCLAIMER_TEXT;
        }

        if (confidence < LOW_CONFIDENCE_THRESHOLD) {
            log.info("Low confidence ({}) detected, adding disclaimer", confidence);
            return answer + DISCLAIMER_TEXT;
        }

        log.debug("Confidence ({}) is acceptable, no disclaimer needed", confidence);
        return answer;
    }
}
