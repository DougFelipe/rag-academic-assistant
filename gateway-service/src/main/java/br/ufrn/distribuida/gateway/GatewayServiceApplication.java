package br.ufrn.distribuida.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Gateway Service Application - API Gateway Reativo.
 * 
 * <p>
 * Este microserviço atua como ponto de entrada único para todos os requests de
 * clientes,
 * fornecendo roteamento centralizado, balanceamento de carga e padrões de
 * resiliência.
 * 
 * <p>
 * <b>Funcionalidades Principais:</b>
 * <ul>
 * <li>Roteamento centralizado para todos os microserviços</li>
 * <li>Descoberta de serviços via Eureka (lb://service-name)</li>
 * <li>Circuit Breaker para tolerância a falhas</li>
 * <li>Retry com backoff exponencial</li>
 * <li>Rate Limiting global</li>
 * <li>Distributed Tracing (Zipkin/Brave)</li>
 * <li>Métricas Prometheus</li>
 * <li>CORS configurável</li>
 * </ul>
 * 
 * <p>
 * <b>Tecnologias:</b>
 * <ul>
 * <li><b>Spring Cloud Gateway MVC</b> - Gateway baseado em Servlet (não
 * reativo)</li>
 * <li><b>Netflix Eureka Client</b> - Descoberta de serviços</li>
 * <li><b>Resilience4j</b> - Circuit Breaker, Retry, Rate Limiter</li>
 * <li><b>Micrometer + Prometheus</b> - Métricas e observabilidade</li>
 * <li><b>Brave</b> - Distributed Tracing (suporte nativo Spring Boot)</li>
 * </ul>
 * 
 * <p>
 * <b>Arquitetura:</b>
 * 
 * <pre>
 * Cliente (JMeter, Browser, etc.)
 *     ↓
 *   HTTP Request
 *     ↓
 * ┌─────────────────────┐
 * │  Gateway Service    │
 * │  Porta: 8080        │
 * │                     │
 * │  Rotas:             │
 * │  ├─ /api/orchestr...│ → lb://orchestrator-service
 * │  ├─ /api/ai/**      │ → lb://ai-service
 * │  └─ /api/function/**│ → lb://function-service
 * │                     │
 * │  Padrões:           │
 * │  ├─ Circuit Breaker │
 * │  ├─ Retry (3x)      │
 * │  ├─ Rate Limit      │
 * │  └─ Tracing         │
 * └─────────────────────┘
 *     ↓
 *   Eureka Discovery
 *     ↓
 * Backend Services
 * </pre>
 * 
 * <p>
 * <b>Fluxo de Request:</b>
 * <ol>
 * <li>Cliente faz request para http://gateway:8080/api/orchestrator/ask</li>
 * <li>Gateway identifica rota /api/orchestrator/**</li>
 * <li>Aplica filtros (StripPrefix, Retry, CircuitBreaker)</li>
 * <li>Consulta Eureka para descobrir orchestrator-service</li>
 * <li>Faz proxy para http://orchestrator:8081/ask</li>
 * <li>Retorna resposta ao cliente</li>
 * <li>Exporta métricas e traces</li>
 * </ol>
 * 
 * <p>
 * <b>Anotações:</b>
 * <ul>
 * <li>{@code @SpringBootApplication} - Habilita auto-configuração do Spring
 * Boot</li>
 * </ul>
 * 
 * <p>
 * <b>Tracing:</b> Propagação automática de trace context para serviços
 * downstream
 * usando Brave (suporte nativo Spring Boot). Traces exportados para Zipkin.
 * 
 * @see org.springframework.cloud.gateway.mvc.config.GatewayMvcAutoConfiguration
 */
@SpringBootApplication
public class GatewayServiceApplication {

	/**
	 * Método principal que inicializa o Gateway Service.
	 * 
	 * <p>
	 * Inicializa o contexto Spring Boot, carrega configurações do Config Server,
	 * registra-se no Eureka, e inicia o servidor Tomcat na porta 8080.
	 * 
	 * <p>
	 * <b>Sequência de Inicialização:</b>
	 * <ol>
	 * <li>Carrega configurações do Config Server (gateway-service.yml)</li>
	 * <li>Registra-se no Eureka Server (porta 8761)</li>
	 * <li>Configura rotas para microserviços</li>
	 * <li>Inicializa Circuit Breakers (orchestrator, ai, function)</li>
	 * <li>Configura filtros globais (Retry, Rate Limit, CORS)</li>
	 * <li>Habilita Distributed Tracing (Brave)</li>
	 * <li>Expõe métricas Prometheus (/actuator/prometheus)</li>
	 * <li>Inicia servidor Tomcat na porta 8080</li>
	 * </ol>
	 * 
	 * <p>
	 * <b>Rotas Configuradas:</b>
	 * <ul>
	 * <li>/api/orchestrator/** → lb://orchestrator-service</li>
	 * <li>/api/ai/** → lb://ai-service</li>
	 * <li>/api/function/** → lb://function-service</li>
	 * </ul>
	 * 
	 * @param args Argumentos de linha de comando (não utilizados)
	 */
	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

}
