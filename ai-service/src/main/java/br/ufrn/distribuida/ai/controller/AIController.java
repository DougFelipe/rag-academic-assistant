package br.ufrn.distribuida.ai.controller;

import br.ufrn.distribuida.ai.dto.AIRequest;
import br.ufrn.distribuida.ai.dto.AIResponse;
import br.ufrn.distribuida.ai.dto.ProfessorInfo;
import br.ufrn.distribuida.ai.dto.ProfessorListResponse;
import br.ufrn.distribuida.ai.service.AIProcessingService;
import br.ufrn.distribuida.ai.service.MCPInternalService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for AI Service - Professor Information Management.
 * 
 * Provides endpoints to:
 * - Query professor data (MCP Internal - Sprint 5)
 * - Ask questions to AI assistant (Spring AI Integration - Sprint 6)
 * 
 * @since Sprint 5 (MCP Internal)
 * @updated Sprint 6 (Spring AI Integration)
 */
@RestController
@RequestMapping("/api/ai")
@Slf4j
public class AIController {
    
    private final MCPInternalService mcpInternalService;
    private final AIProcessingService aiProcessingService;
    
    @Autowired
    public AIController(
            MCPInternalService mcpInternalService,
            AIProcessingService aiProcessingService) {
        this.mcpInternalService = mcpInternalService;
        this.aiProcessingService = aiProcessingService;
    }
    
    @GetMapping("/professors")
    public ResponseEntity<ProfessorListResponse> listProfessors() {
        log.info("GET /api/ai/professors - Listing all professors");
        
        List<String> names = mcpInternalService.getAllProfessorNames();
        return ResponseEntity.ok(ProfessorListResponse.of(names));
    }
    
    @GetMapping("/professors/{nameOrId}")
    public ResponseEntity<ProfessorInfo> getProfessor(@PathVariable String nameOrId) {
        log.info("GET /api/ai/professors/{} - Getting professor info", nameOrId);
        
        ProfessorInfo info = mcpInternalService.getProfessorInfo(nameOrId);
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/professors/search")
    public ResponseEntity<List<ProfessorInfo>> searchProfessors(@RequestParam String keyword) {
        log.info("GET /api/ai/professors/search?keyword={} - Searching professors", keyword);
        
        List<ProfessorInfo> results = mcpInternalService.searchByResearchArea(keyword);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/professors/all")
    public ResponseEntity<List<ProfessorInfo>> getAllProfessorsDetails() {
        log.info("GET /api/ai/professors/all - Getting all professors with details");
        
        List<ProfessorInfo> professors = mcpInternalService.getAllProfessors();
        return ResponseEntity.ok(professors);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        int count = mcpInternalService.getProfessorCount();
        String message = String.format("AI Service is healthy. Loaded %d professors.", count);
        log.debug("GET /api/ai/health - {}", message);
        return ResponseEntity.ok(message);
    }
    
    // ==========================================
    // SPRINT 6: Spring AI Integration Endpoints
    // ==========================================
    
    /**
     * Ask a question to the AI assistant.
     * 
     * Uses RAG (Retrieval-Augmented Generation) to provide contextual answers
     * based on professor data. The AI can also call tools to fetch additional
     * information.
     * 
     * @param request The AI request containing the question
     * @return CompletableFuture with AI response
     * @since Sprint 6
     */
    @PostMapping("/ask")
    public CompletableFuture<ResponseEntity<AIResponse>> askQuestion(
            @RequestBody @Valid AIRequest request) {
        
        log.info("POST /api/ai/ask - Processing question: '{}'", request.getQuestion());
        
        return aiProcessingService.processQuestion(request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("❌ Error processing AI question: {}", ex.getMessage());
                    
                    AIResponse errorResponse = AIResponse.builder()
                            .answer("Erro ao processar pergunta. Por favor, tente novamente.")
                            .sources(List.of())
                            .confidence(0.0)
                            .processingTimeMs(0L)
                            .model("error")
                            .build();
                    
                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(errorResponse);
                });
    }
    
    /**
     * List available AI models and current configuration.
     * 
     * @return Information about AI models
     * @since Sprint 6
     */
    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> listModels() {
        log.info("GET /api/ai/models - Listing available models");
        
        Map<String, Object> models = Map.of(
                "active", "gpt-4-turbo-preview",
                "available", List.of(
                        "gpt-4-turbo-preview",
                        "gpt-4",
                        "gpt-3.5-turbo",
                        "ollama-llama2"
                ),
                "provider", "openai",
                "features", List.of(
                        "RAG (Retrieval-Augmented Generation)",
                        "Function Calling (@Tool support)",
                        "Circuit Breaker",
                        "Retry with exponential backoff",
                        "Timeout protection"
                )
        );
        
        return ResponseEntity.ok(models);
    }
}
