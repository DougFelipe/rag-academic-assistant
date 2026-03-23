package br.ufrn.distribuida.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Classe Principal do AI Service - Microserviço de Inteligência Artificial.
 * 
 * <p>
 * Este microserviço é responsável por:
 * <ul>
 * <li>Gerenciar informações de professores (MCP Internal Server)</li>
 * <li>Processar perguntas usando IA com RAG (Retrieval-Augmented
 * Generation)</li>
 * <li>Integrar com LLMs (Ollama/OpenAI) via Spring AI</li>
 * <li>Fornecer Function Calling para a IA buscar dados dinamicamente</li>
 * </ul>
 * 
 * <p>
 * <b>Anotações:</b>
 * <ul>
 * <li>{@code @SpringBootApplication} - Configura auto-configuração Spring
 * Boot</li>
 * <li>{@code @EnableDiscoveryClient} - Habilita registro no Eureka Server</li>
 * </ul>
 * 
 * <p>
 * <b>Arquitetura:</b>
 * 
 * <pre>
 * Gateway → AI Service → Ollama/OpenAI
 *              ↓
 *         MCP Internal
 *              ↓
 *       Professor Repository
 * </pre>
 * 
 * @author Equipe DISTRIBUIDA 3
 * @see br.ufrn.distribuida.ai.controller.AIController
 * @see br.ufrn.distribuida.ai.service.AIProcessingService
 * @see br.ufrn.distribuida.ai.service.MCPInternalService
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AiServiceApplication {

	/**
	 * Método principal que inicializa a aplicação Spring Boot.
	 * 
	 * <p>
	 * Inicializa o contexto Spring, registra o serviço no Eureka,
	 * carrega configurações do Config Server e inicia o servidor web
	 * na porta 8082.
	 * 
	 * @param args Argumentos da linha de comando (não utilizados)
	 */
	public static void main(String[] args) {
		SpringApplication.run(AiServiceApplication.class, args);
	}

}
