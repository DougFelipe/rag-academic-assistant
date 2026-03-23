package br.ufrn.distribuida.function.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO para enriquecimento de respostas.
 * 
 * <p>
 * Utilizado pela função AnswerEnricher para adicionar metadados,
 * formatar respostas e incluir disclaimers quando necessário.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDTO {

    /**
     * Texto da resposta (original ou enriquecido).
     */
    private String answer;

    /**
     * Lista de fontes utilizadas para gerar a resposta.
     */
    @Builder.Default
    private List<String> sources = new ArrayList<>();

    /**
     * Nível de confiança da resposta (0.0 a 1.0).
     */
    private Double confidence;

    /**
     * Metadados adicionais da resposta.
     * Pode incluir timestamps, versão, informações de processamento, etc.
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
