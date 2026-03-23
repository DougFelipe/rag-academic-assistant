package br.ufrn.distribuida.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de Requisição para Processamento de Perguntas pela IA.
 * 
 * <p>
 * Este DTO é utilizado quando clientes desejam fazer perguntas ao assistente
 * de IA sobre professores, áreas de pesquisa ou qualquer informação relacionada
 * ao IMD/BIOME.
 * 
 * <p>
 * <b>Exemplo de Uso:</b>
 * 
 * <pre>
 * AIRequest request = AIRequest.builder()
 *         .question("Quais professores pesquisam Inteligência Artificial?")
 *         .context("Preciso encontrar orientador para mestrado")
 *         .build();
 * </pre>
 * 
 * <p>
 * <b>Validações:</b>
 * <ul>
 * <li>{@code question} - Campo obrigatório, não pode ser vazio</li>
 * <li>{@code context} - Opcional, fornece contexto adicional</li>
 * <li>{@code professorName} - Opcional, foca a busca em um professor
 * específico</li>
 * </ul>
 * 
 * @author Equipe DISTRIBUIDA 3
 * @version 1.0.0
 * @since Sprint 6 (Spring AI Integration)
 * @see AIResponse
 * @see br.ufrn.distribuida.ai.service.AIProcessingService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {

    /**
     * A pergunta a ser processada pela IA.
     * 
     * <p>
     * Campo obrigatório. Deve conter a pergunta que o usuário deseja fazer
     * ao assistente de IA. A pergunta será processada usando RAG (Retrieval-
     * Augmented Generation) para fornecer respostas contextualizadas.
     * 
     * @see br.ufrn.distribuida.ai.service.AIProcessingService#processQuestion(AIRequest)
     */
    @NotBlank(message = "Question cannot be blank")
    private String question;

    /**
     * Contexto adicional para a pergunta (opcional).
     * 
     * <p>
     * Pode ser usado para fornecer informações mais específicas ou restrições
     * que ajudem a IA a gerar uma resposta mais precisa. Por exemplo: "Preciso
     * de orientador para mestrado em IA" ou "Foco em Deep Learning".
     */
    private String context;

    /**
     * Nome específico de professor para focar a busca (opcional).
     * 
     * <p>
     * Se fornecido, a IA priorizará informações sobre este professor específico
     * ao gerar a resposta. Útil para perguntas direcionadas como "Quais são as
     * áreas de pesquisa do Prof. João Silva?".
     */
    private String professorName;
}
