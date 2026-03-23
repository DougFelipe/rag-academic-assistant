package br.ufrn.distribuida.mcpexternal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * MCP External Service Application - Servidor MCP de Terceiros.
 *
 * <p>Este microserviço integra com a API OpenWeatherMap para fornecer
 * informações meteorológicas que podem enriquecer respostas da IA.
 *
 * <p><b>Funcionalidades Principais:</b>
 * <ul>
 *   <li>Integração com OpenWeatherMap API via Feign Client</li>
 *   <li>Fornece clima atual para qualquer cidade</li>
 *   <li>Suporta múltiplas unidades (Celsius, Fahrenheit, Kelvin)</li>
 *   <li>Suporta múltiplos idiomas (pt_br, en, es, etc.)</li>
 *   <li>Registra-se no Eureka para descoberta</li>
 * </ul>
 *
 * <p><b>Arquitetura MCP (Model Context Protocol):</b>
 * <pre>
 * AI Service
 *     ↓
 *   Pergunta: "Qual o clima em Natal?"
 *     ↓
 * ┌─────────────────────┐
 * │  MCP External       │
 * │  Service            │
 * │  Porta: 8084        │
 * │                     │
 * │  Endpoints:         │
 * │  GET /weather/{city}│
 * │  POST /weather      │
 * └─────────┬───────────┘
 *           │
 *           │ Feign Client
 *           ▼
 * ┌─────────────────────┐
 * │  OpenWeatherMap API │
 * │  api.openweather... │
 * └─────────────────────┘
 * </pre>
 *
 * <p><b>Anotações:</b>
 * <ul>
 *   <li>{@code @SpringBootApplication} - Habilita auto-configuração</li>
 *   <li>{@code @EnableDiscoveryClient} - Registra no Eureka</li>
 *   <li>{@code @EnableFeignClients} - Habilita Feign para chamadas HTTP</li>
 * </ul>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class McpExternalApplication {

    /**
     * Método principal que inicializa o MCP External Service.
     *
     * <p>Inicializa o contexto Spring Boot, registra-se no Eureka,
     * e configura Feign Client para OpenWeatherMap.
     *
     * @param args Argumentos de linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        SpringApplication.run(McpExternalApplication.class, args);
    }
}
