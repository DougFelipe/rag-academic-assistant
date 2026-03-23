package br.ufrn.distribuida.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de Resposta para Processamento de Perguntas pela IA.
 * 
 * <p>
 * Contém a resposta gerada pela IA junto com metadados sobre o processamento,
 * fontes utilizadas e nível de confiança da resposta.
 * 
 * <p>
 * <b>Exemplo de Resposta:</b>
 * 
 * <pre>
 * AIResponse response = AIResponse.builder()
 *         .answer("Os professores que pesquisam IA são: João Silva e Maria Santos")
 *         .sources(List.of("Prof. João Silva", "Prof. Maria Santos"))
 *         .confidence(0.85)
 *         .processingTimeMs(12340L)
 *         .model("ollama-llama2")
 *         .build();
 * </pre>
 * 
 * <p>
 * <b>Campos:</b>
 * <ul>
 * <li>{@code answer} - Resposta gerada pela IA</li>
 * <li>{@code sources} - Lista de fontes utilizadas (nomes de professores)</li>
 * <li>{@code confidence} - Nível de confiança (0.0 a 1.0)</li>
 * <li>{@code processingTimeMs} - Tempo de processamento em milissegundos</li>
 * <li>{@code model} - Modelo de IA utilizado</li>
 * </ul>
 * 
 * @author Equipe DISTRIBUIDA 3
 * @version 1.0.0
 * @since Sprint 6 (Spring AI Integration)
 * @see AIRequest
 * @see br.ufrn.distribuida.ai.service.AIProcessingService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {

    /**
     * A resposta gerada pela IA para a pergunta do usuário.
     * 
     * <p>
     * Contém a resposta em linguagem natural gerada pelo modelo de IA
     * (Ollama ou OpenAI) com base no contexto enriquecido de professores.
     */
    private String answer;

    /**
     * Lista de fontes utilizadas para gerar a resposta.
     * 
     * <p>
     * Tipicamente contém nomes de professores ou títulos de documentos
     * que foram utilizados como contexto para a geração da resposta.
     * Útil para rastreabilidade e verificação das informações.
     * 
     * <p>
     * Valor padrão: lista vazia
     */
    @Builder.Default
    private List<String> sources = List.of();

    /**
     * Nível de confiança da resposta (0.0 a 1.0).
     * 
     * <p>
     * Valores mais altos indicam respostas mais confiáveis. O cálculo
     * considera fatores como:
     * <ul>
     * <li>Quantidade de contexto disponível</li>
     * <li>Relevância das fontes encontradas</li>
     * <li>Comprimento e qualidade da resposta</li>
     * </ul>
     * 
     * <p>
     * <b>Interpretação:</b>
     * <ul>
     * <li>0.8 - 1.0: Alta confiança</li>
     * <li>0.5 - 0.8: Confiança moderada</li>
     * <li>0.0 - 0.5: Baixa confiança</li>
     * </ul>
     */
    private Double confidence;

    /**
     * Tempo de processamento em milissegundos.
     * 
     * <p>
     * Útil para monitoramento de performance e análise de latência.
     * Inclui o tempo total desde o recebimento da requisição até a
     * geração completa da resposta.
     */
    private Long processingTimeMs;

    /**
     * Modelo de IA utilizado para geração da resposta.
     * 
     * <p>
     * Exemplos:
     * <ul>
     * <li>"ollama-llama2" - Modelo Ollama local</li>
     * <li>"gpt-4-turbo-preview" - OpenAI GPT-4 Turbo</li>
     * <li>"gpt-3.5-turbo" - OpenAI GPT-3.5</li>
     * <li>"fallback" - Resposta de fallback (sem IA)</li>
     * </ul>
     */
    private String model;
}
