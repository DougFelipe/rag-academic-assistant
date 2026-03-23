package br.ufrn.distribuida.function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Function Service Application - Serviço de Funções para Spring AI.
 * 
 * <p>
 * Este microserviço fornece funções reutilizáveis para o Spring AI através
 * do padrão Function Calling. Implementa funções de pré-processamento de
 * perguntas
 * e enriquecimento de respostas que podem ser chamadas pelo AI Service.
 * 
 * <p>
 * <b>Funções Implementadas:</b>
 * <ul>
 * <li><b>preprocessQuestion</b> - Pré-processa perguntas (normalização,
 * extração de professor, detecção de intenção)</li>
 * <li><b>enrichAnswer</b> - Enriquece respostas (metadados, formatação,
 * disclaimers)</li>
 * </ul>
 * 
 * <p>
 * <b>Anotações:</b>
 * <ul>
 * <li>{@code @SpringBootApplication} - Habilita auto-configuração do Spring
 * Boot</li>
 * <li>{@code exclude} - Desabilita Zipkin (tracing não necessário para funções
 * stateless)</li>
 * </ul>
 * 
 * <p>
 * <b>Arquitetura:</b>
 * 
 * <pre>
 * Orchestrator
 *     ↓
 *   Chama
 *     ↓
 * ┌─────────────────────┐
 * │  Function Service   │
 * │  Porta: 8083        │
 * │                     │
 * │  Funções:           │
 * │  ├─ preprocess      │
 * │  │  Question        │
 * │  └─ enrich          │
 * │     Answer          │
 * └─────────────────────┘
 * </pre>
 * 
 * <p>
 * <b>Fluxo de Uso:</b>
 * <ol>
 * <li>Orchestrator recebe pergunta do usuário</li>
 * <li>Chama preprocessQuestion para normalizar</li>
 * <li>Envia pergunta processada para AI Service</li>
 * <li>Recebe resposta da IA</li>
 * <li>Chama enrichAnswer para adicionar metadados</li>
 * <li>Retorna resposta enriquecida ao usuário</li>
 * </ol>
 * 
 * @see br.ufrn.distribuida.function.functions.QuestionPreprocessor
 * @see br.ufrn.distribuida.function.functions.AnswerEnricher
 */
@SpringBootApplication(exclude = {
		org.springframework.boot.actuate.autoconfigure.tracing.zipkin.ZipkinAutoConfiguration.class
})
public class FunctionServiceApplication {

	/**
	 * Método principal que inicializa o Function Service.
	 * 
	 * <p>
	 * Inicializa o contexto Spring Boot, registra funções Spring Cloud Function,
	 * e expõe endpoints HTTP para chamada das funções.
	 * 
	 * <p>
	 * <b>Sequência de Inicialização:</b>
	 * <ol>
	 * <li>Carrega configurações do Config Server</li>
	 * <li>Registra-se no Eureka Server (porta 8761)</li>
	 * <li>Detecta funções anotadas com @Component</li>
	 * <li>Expõe funções via HTTP (POST /preprocessQuestion, POST
	 * /enrichAnswer)</li>
	 * <li>Inicia servidor Tomcat na porta 8083</li>
	 * </ol>
	 * 
	 * @param args Argumentos de linha de comando (não utilizados)
	 */
	public static void main(String[] args) {
		SpringApplication.run(FunctionServiceApplication.class, args);
	}

}
