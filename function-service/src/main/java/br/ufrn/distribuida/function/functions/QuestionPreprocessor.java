package br.ufrn.distribuida.function.functions;

import br.ufrn.distribuida.function.dto.QuestionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Função para pré-processamento de perguntas antes do processamento pela IA.
 * 
 * <p>
 * <b>Responsabilidades:</b>
 * <ol>
 * <li>Extrair nome de professor da pergunta usando regex</li>
 * <li>Normalizar texto (remover acentos, lowercase, trim)</li>
 * <li>Detectar intenção (RESEARCH_QUERY, COURSE_QUERY, CONTACT_QUERY,
 * GENERAL_QUERY)</li>
 * </ol>
 * 
 * <p>
 * Esta função é exposta como Spring Cloud Function e pode ser chamada via HTTP:
 * POST /preprocessQuestion
 */
@Component("preprocessQuestion")
@Slf4j
public class QuestionPreprocessor implements Function<QuestionDTO, QuestionDTO> {

    // Regex patterns for professor name extraction
    private static final Pattern PROFESSOR_PATTERN = Pattern.compile(
            "(?:professor|professora|prof\\.?)\\s+([A-ZÀ-Ú][a-zà-ú]+(?:\\s+[A-ZÀ-Ú][a-zà-ú]+)*)",
            Pattern.CASE_INSENSITIVE);

    // Keywords for intent detection
    private static final String[] RESEARCH_KEYWORDS = {
            "pesquisa", "research", "área", "area", "publicação", "publication",
            "artigo", "article", "projeto", "project"
    };

    private static final String[] COURSE_KEYWORDS = {
            "disciplina", "course", "matéria", "subject", "aula", "class",
            "ensina", "teach", "leciona", "IMD", "PPgSC"
    };

    private static final String[] CONTACT_KEYWORDS = {
            "contato", "contact", "email", "telefone", "phone", "sala", "office",
            "horário", "schedule", "atendimento"
    };

    @Override
    public QuestionDTO apply(QuestionDTO input) {
        log.info("Preprocessing question: {}", input.getQuestion());

        long startTime = System.currentTimeMillis();

        // 1. Extract professor name (if not already provided)
        String professorName = input.getProfessorName();
        if (professorName == null || professorName.isBlank()) {
            professorName = extractProfessorName(input.getQuestion());
        }

        // 2. Normalize text
        String normalizedQuestion = normalizeText(input.getQuestion());

        // 3. Detect intent
        String intent = detectIntent(normalizedQuestion);

        // 4. Build metadata
        Map<String, Object> metadata = new HashMap<>(input.getMetadata());
        metadata.put("preprocessed_at", System.currentTimeMillis());
        metadata.put("processing_time_ms", System.currentTimeMillis() - startTime);
        metadata.put("original_question", input.getQuestion());
        metadata.put("professor_extracted", professorName != null && !professorName.isBlank());

        QuestionDTO result = QuestionDTO.builder()
                .question(normalizedQuestion)
                .professorName(professorName)
                .intent(intent)
                .metadata(metadata)
                .build();

        log.info("Question preprocessed - Intent: {}, Professor: {}, Time: {}ms",
                intent, professorName, metadata.get("processing_time_ms"));

        return result;
    }

    /**
     * Extrai nome de professor do texto da pergunta usando regex.
     * 
     * <p>
     * <b>Exemplos:</b>
     * <ul>
     * <li>"Quem é o professor Marcel Oliveira?" → "Marcel Oliveira"</li>
     * <li>"O Prof. João Silva leciona qual disciplina?" → "João Silva"</li>
     * <li>"Sobre a pesquisa da professora Ana Costa" → "Ana Costa"</li>
     * </ul>
     * 
     * @param question Texto da pergunta
     * @return Nome do professor extraído ou string vazia se não encontrado
     */
    private String extractProfessorName(String question) {
        if (question == null || question.isBlank()) {
            return "";
        }

        Matcher matcher = PROFESSOR_PATTERN.matcher(question);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            log.debug("Extracted professor name: {}", name);
            return name;
        }

        log.debug("No professor name found in question");
        return "";
    }

    /**
     * Normaliza texto removendo acentos, convertendo para minúsculas e fazendo
     * trim.
     * 
     * <p>
     * Garante processamento consistente de texto para modelos de IA.
     * 
     * @param text Texto a normalizar
     * @return Texto normalizado
     */
    private String normalizeText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        // Remove accents using NFD normalization
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);

        // Remove diacritical marks
        normalized = normalized.replaceAll("\\p{M}", "");

        // Convert to lowercase and trim
        normalized = normalized.toLowerCase().trim();

        // Replace multiple spaces with single space
        normalized = normalized.replaceAll("\\s+", " ");

        log.debug("Text normalized: {} -> {}", text, normalized);
        return normalized;
    }

    /**
     * Detecta a intenção da pergunta baseado em palavras-chave.
     * 
     * <p>
     * <b>Intenções:</b>
     * <ul>
     * <li><b>RESEARCH_QUERY:</b> Perguntas sobre áreas de pesquisa, publicações,
     * projetos</li>
     * <li><b>COURSE_QUERY:</b> Perguntas sobre cursos, disciplinas, ensino</li>
     * <li><b>CONTACT_QUERY:</b> Perguntas sobre contato, horários, localização</li>
     * <li><b>GENERAL_QUERY:</b> Outras perguntas gerais</li>
     * </ul>
     * 
     * @param question Texto da pergunta normalizado
     * @return Intenção detectada
     */
    private String detectIntent(String question) {
        if (question == null || question.isBlank()) {
            return "GENERAL_QUERY";
        }

        String lowerQuestion = question.toLowerCase();

        // Check for research keywords
        for (String keyword : RESEARCH_KEYWORDS) {
            if (lowerQuestion.contains(keyword.toLowerCase())) {
                log.debug("Detected RESEARCH_QUERY intent (keyword: {})", keyword);
                return "RESEARCH_QUERY";
            }
        }

        // Check for course keywords
        for (String keyword : COURSE_KEYWORDS) {
            if (lowerQuestion.contains(keyword.toLowerCase())) {
                log.debug("Detected COURSE_QUERY intent (keyword: {})", keyword);
                return "COURSE_QUERY";
            }
        }

        // Check for contact keywords
        for (String keyword : CONTACT_KEYWORDS) {
            if (lowerQuestion.contains(keyword.toLowerCase())) {
                log.debug("Detected CONTACT_QUERY intent (keyword: {})", keyword);
                return "CONTACT_QUERY";
            }
        }

        log.debug("No specific intent detected, using GENERAL_QUERY");
        return "GENERAL_QUERY";
    }
}
