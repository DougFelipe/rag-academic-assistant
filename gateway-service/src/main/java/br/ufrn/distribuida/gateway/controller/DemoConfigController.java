package br.ufrn.distribuida.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller de demonstração para Config Refresh.
 * 
 * <p>
 * Este controller demonstra como propriedades podem ser atualizadas
 * dinamicamente via Config Server sem reiniciar o serviço.
 * 
 * <p>
 * <b>Como testar:</b>
 * <ol>
 * <li>GET /demo/config → Ver valores atuais</li>
 * <li>Editar config-repo/gateway-service.yml</li>
 * <li>Commit no Git</li>
 * <li>POST /actuator/refresh</li>
 * <li>GET /demo/config → Ver novos valores (SEM RESTART!)</li>
 * </ol>
 * 
 * <p>
 * <b>Anotação @RefreshScope:</b> Permite que o bean seja recriado
 * quando /actuator/refresh é chamado, pegando novos valores do Config Server.
 */
@RestController
@RequestMapping("/demo")
@RefreshScope // ← CRÍTICO: Permite refresh sem restart
public class DemoConfigController {

    /**
     * Mensagem customizada do Config Server.
     * Pode ser alterada em config-repo/gateway-service.yml
     */
    @Value("${demo.message:Mensagem padrão}")
    private String message;

    /**
     * Flag de feature toggle.
     * Pode ser alterada em config-repo/gateway-service.yml
     */
    @Value("${demo.feature-enabled:false}")
    private boolean featureEnabled;

    /**
     * Limite de requests.
     * Pode ser alterado em config-repo/gateway-service.yml
     */
    @Value("${demo.max-requests:100}")
    private int maxRequests;

    /**
     * Endpoint de demonstração que retorna configurações atuais.
     * 
     * <p>
     * <b>Teste via Postman:</b>
     * 
     * <pre>
     * GET http://localhost:8080/demo/config
     * </pre>
     * 
     * @return Map com configurações atuais e timestamp
     */
    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("message", message);
        config.put("featureEnabled", featureEnabled);
        config.put("maxRequests", maxRequests);
        config.put("timestamp", LocalDateTime.now().toString());
        config.put("info", "✅ Valores carregados do Config Server");

        return config;
    }

    /**
     * Endpoint que usa a feature flag.
     * 
     * <p>
     * Demonstra como uma feature pode ser habilitada/desabilitada
     * dinamicamente via Config Server.
     * 
     * @return Mensagem indicando se feature está habilitada
     */
    @GetMapping("/feature")
    public Map<String, Object> checkFeature() {
        Map<String, Object> response = new HashMap<>();

        if (featureEnabled) {
            response.put("status", "ENABLED");
            response.put("message", "✅ Feature está HABILITADA!");
            response.put("action", "Executando funcionalidade...");
        } else {
            response.put("status", "DISABLED");
            response.put("message", "❌ Feature está DESABILITADA!");
            response.put("action", "Funcionalidade não disponível");
        }

        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}
