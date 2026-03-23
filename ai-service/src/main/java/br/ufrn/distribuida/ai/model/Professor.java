package br.ufrn.distribuida.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade Professor representando informações de docentes carregadas de
 * arquivos Markdown.
 * 
 * <p>
 * Esta classe modela os dados de professores do IMD/BIOME que são carregados
 * a partir de arquivos Markdown com frontmatter YAML. Os dados são mantidos em
 * memória e utilizados tanto para consultas diretas quanto para enriquecimento
 * de contexto em perguntas à IA (RAG).
 * 
 * <p>
 * <b>Estrutura do Arquivo Markdown:</b>
 * 
 * <pre>
 * ---
 * id: prof-001
 * name: João Silva
 * department: IMD
 * email: joao@ufrn.br
 * ---
 * 
 * # João Silva
 * 
 * ## Áreas de Pesquisa
 * - Inteligência Artificial
 * - Machine Learning
 * </pre>
 * 
 * <p>
 * <b>Uso:</b>
 * <ul>
 * <li>MCP Internal Server - Fornece dados via API REST</li>
 * <li>RAG (Retrieval-Augmented Generation) - Contexto para IA</li>
 * <li>Function Calling - Dados para ferramentas da IA</li>
 * </ul>
 * 
 * @author Equipe DISTRIBUIDA 3
 * @version 1.0.0
 * @since Sprint 5 (MCP Internal)
 * @see br.ufrn.distribuida.ai.repository.ProfessorRepository
 * @see br.ufrn.distribuida.ai.parser.MarkdownParser
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Professor {

    /**
     * Identificador único do professor (extraído do frontmatter YAML).
     * 
     * <p>
     * Formato: "prof-XXX" onde XXX é um número sequencial.
     * Exemplo: "prof-001", "prof-002"
     */
    private String id;

    /**
     * Nome completo do professor.
     * 
     * <p>
     * Utilizado para buscas e exibição. Deve ser único no sistema.
     */
    private String name;

    /**
     * Departamento onde o professor trabalha.
     * 
     * <p>
     * Exemplos: "IMD", "BIOME", "DCA"
     */
    private String department;

    /**
     * Endereço de e-mail institucional do professor.
     * 
     * <p>
     * Formato: usuario@ufrn.br
     */
    private String email;

    /**
     * Localização do escritório do professor.
     * 
     * <p>
     * Exemplos: "Sala 201 - IMD", "Laboratório de IA"
     */
    private String office;

    /**
     * Número de telefone ou ramal do professor.
     * 
     * <p>
     * Pode conter telefone completo ou apenas ramal interno.
     */
    private String phone;

    /**
     * Conteúdo completo em Markdown incluindo biografia, áreas de pesquisa, cursos,
     * etc.
     * 
     * <p>
     * Este campo contém todo o conteúdo do arquivo Markdown após o frontmatter.
     * É utilizado para:
     * <ul>
     * <li>Extração de áreas de pesquisa</li>
     * <li>Busca por keywords</li>
     * <li>Contexto para RAG (Retrieval-Augmented Generation)</li>
     * </ul>
     * 
     * <p>
     * O conteúdo é indexado para permitir buscas eficientes por termos-chave.
     */
    private String content;

    /**
     * Nome do arquivo original de onde o professor foi carregado.
     * 
     * <p>
     * Útil para debugging e rastreamento da origem dos dados.
     * Exemplo: "joao-silva.md"
     */
    private String sourceFile;
}
