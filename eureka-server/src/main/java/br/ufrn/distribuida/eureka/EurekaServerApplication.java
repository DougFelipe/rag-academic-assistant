package br.ufrn.distribuida.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server Application - Servidor de Descoberta de Serviços.
 * 
 * <p>
 * Este microserviço implementa o Netflix Eureka Server, fornecendo
 * descoberta de serviços (Service Discovery) para toda a arquitetura
 * distribuída.
 * Permite que os microserviços se registrem e descubram uns aos outros
 * dinamicamente,
 * eliminando a necessidade de configuração estática de endpoints.
 * 
 * <p>
 * <b>Funcionalidades Principais:</b>
 * <ul>
 * <li>Registro automático de microserviços</li>
 * <li>Descoberta dinâmica de serviços</li>
 * <li>Health checks periódicos</li>
 * <li>Balanceamento de carga (via Ribbon/LoadBalancer)</li>
 * <li>Self-preservation mode (proteção contra falhas de rede)</li>
 * </ul>
 * 
 * <p>
 * <b>Anotações:</b>
 * <ul>
 * <li>{@code @SpringBootApplication} - Habilita auto-configuração do Spring
 * Boot</li>
 * <li>{@code @EnableEurekaServer} - Ativa o servidor Eureka</li>
 * </ul>
 * 
 * <p>
 * <b>Arquitetura:</b>
 * 
 * <pre>
 * Microserviços (Clientes Eureka)
 *     ↓
 *   Registro
 *     ↓
 * ┌─────────────────────┐
 * │  Eureka Server      │
 * │  Porta: 8761        │
 * │                     │
 * │  Registry:          │
 * │  ├─ gateway-service │
 * │  ├─ orchestrator    │
 * │  ├─ ai-service      │
 * │  └─ function-svc    │
 * └─────────────────────┘
 *     ↑
 *   Consulta
 *     ↑
 * Microserviços (Descoberta)
 * </pre>
 * 
 * <p>
 * <b>Fluxo de Registro:</b>
 * <ol>
 * <li>Microserviço inicia e se registra no Eureka Server</li>
 * <li>Eureka Server armazena informações do serviço (IP, porta, health)</li>
 * <li>Microserviço envia heartbeats periódicos (a cada 30s)</li>
 * <li>Outros serviços consultam Eureka para descobrir endpoints</li>
 * <li>Gateway usa Eureka para roteamento dinâmico (lb://service-name)</li>
 * </ol>
 * 
 * @see org.springframework.cloud.netflix.eureka.server.EnableEurekaServer
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

	/**
	 * Método principal que inicializa o Eureka Server.
	 * 
	 * <p>
	 * Inicializa o contexto Spring Boot, inicia o servidor Tomcat embutido
	 * na porta 8761, e começa a aceitar registros de microserviços.
	 * 
	 * <p>
	 * <b>Sequência de Inicialização:</b>
	 * <ol>
	 * <li>Carrega configurações do Config Server (application.yml,
	 * eureka-server.yml)</li>
	 * <li>Inicializa servidor Eureka na porta 8761</li>
	 * <li>Configura modo standalone (não se registra em outro Eureka)</li>
	 * <li>Habilita self-preservation mode</li>
	 * <li>Expõe dashboard web em http://localhost:8761</li>
	 * <li>Aguarda registro de microserviços clientes</li>
	 * </ol>
	 * 
	 * <p>
	 * <b>Configurações Importantes:</b>
	 * <ul>
	 * <li>register-with-eureka: false (não se auto-registra)</li>
	 * <li>fetch-registry: false (não busca de outro Eureka)</li>
	 * <li>enable-self-preservation: true (proteção contra falhas de rede)</li>
	 * </ul>
	 * 
	 * @param args Argumentos de linha de comando (não utilizados)
	 */
	public static void main(String[] args) {
		SpringApplication.run(EurekaServerApplication.class, args);
	}

}
