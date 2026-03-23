package br.ufrn.distribuida.function.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO para pré-processamento de perguntas.
 * 
 * <p>
 * Utilizado pela função QuestionPreprocessor para normalizar perguntas,
 * extrair nomes de professores e detectar intenção.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    /**
     * Texto da pergunta (original ou normalizado).
     */
    @NotBlank(message = "Question cannot be blank")
    private String question;

    /**
     * Nome do professor extraído da pergunta.
     * Pode ser null se nenhum nome for detectado.
     */
    private String professorName;

    /**
     * Intenção detectada da pergunta.
     * Valores possíveis: RESEARCH_QUERY, COURSE_QUERY, CONTACT_QUERY, GENERAL_QUERY
     */
    private String intent;

    /**
     * Metadados adicionais da pergunta.
     * Pode incluir timestamps de processamento, scores de confiança, etc.
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
