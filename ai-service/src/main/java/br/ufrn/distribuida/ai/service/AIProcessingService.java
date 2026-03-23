package br.ufrn.distribuida.ai.service;

import br.ufrn.distribuida.ai.dto.AIRequest;
import br.ufrn.distribuida.ai.dto.AIResponse;
import br.ufrn.distribuida.ai.dto.ProfessorInfo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI Processing Service - Spring AI Integration with RAG.
 * 
 * This service implements Retrieval-Augmented Generation (RAG) using a simple
 * approach:
 * 1. Extract keywords from user question
 * 2. Retrieve relevant professors from MCPInternalService
 * 3. Build enriched context with professor data
 * 4. Send context + question to LLM via ChatClient
 * 5. LLM can also call @Tool methods for additional info
 * 
 * Includes Resilience4j patterns:
 * - Circuit Breaker: Stops requests if AI service fails repeatedly
 * - Retry: Retries failed requests with exponential backoff
 * - Time Limiter: Ensures requests don't hang indefinitely
 * 
 * @RefreshScope enables dynamic configuration updates via POST
 *               /actuator/refresh
 * 
 * @since Sprint 6
 */
@Service
@RefreshScope // Allows configuration refresh without restart
@Slf4j
public class AIProcessingService {

    private final ChatClient chatClient;
    private final MCPInternalService mcpService;

    @Value("${spring.ai.openai.chat.options.model:}")
    private String openaiModel;

    @Value("${spring.ai.ollama.chat.options.model:}")
    private String ollamaModel;

    @Value("${spring.ai.enabled:true}")
    private boolean aiEnabled;

    private String getActiveModel() {
        return (ollamaModel != null && !ollamaModel.isEmpty()) ? ollamaModel : openaiModel;
    }

    /**
     * Constructor with ChatClient.Builder injection.
     * Spring AI auto-configures ChatClient.Builder based on application.yml
     * settings.
     * Tools are auto-discovered via @Bean definitions.
     */
    public AIProcessingService(
            ChatClient.Builder chatClientBuilder,
            MCPInternalService mcpService) {

        this.chatClient = chatClientBuilder.build();
        this.mcpService = mcpService;

        log.info("✅ AIProcessingService initialized with model: {}", getActiveModel());
    }

    /**
     * Process a question using AI with RAG and Resilience4j patterns.
     * 
     * @param request The AI request containing the question
     * @return CompletableFuture with AIResponse
     */
    @CircuitBreaker(name = "aiClient", fallbackMethod = "fallbackProcessQuestion")
    @Retry(name = "aiClient")
    @TimeLimiter(name = "aiClient")
    public CompletableFuture<AIResponse> processQuestion(AIRequest request) {
        if (!aiEnabled) {
            log.warn(" AI Service disabled, returning mock response");
            return CompletableFuture.completedFuture(createMockResponse(request));
        }

        log.info(" Processing AI question: '{}'", request.getQuestion());
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: RAG - Retrieve relevant professors
            List<ProfessorInfo> relevantProfessors = retrieveRelevantProfessors(request);
            log.debug(" RAG retrieved {} relevant professors", relevantProfessors.size());

            // Step 2: Build enriched context
            String enrichedContext = buildEnrichedContext(relevantProfessors, request);

            // Step 3: Build system prompt with context
            String systemPrompt = buildSystemPrompt(enrichedContext);

            // Step 4: Build user prompt
            String userPrompt = buildUserPrompt(request);

            // Step 5: Call LLM with tools available
            log.debug(" Calling ChatClient with {} tools", 3);
            String aiResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .functions("professorInfo", "listProfessors", "searchByResearchArea")
                    .call()
                    .content();

            long processingTime = System.currentTimeMillis() - startTime;
            log.info(" AI response generated in {}ms", processingTime);

            // Step 6: Build response with metadata
            return CompletableFuture.completedFuture(
                    AIResponse.builder()
                            .answer(aiResponse)
                            .sources(extractSources(relevantProfessors))
                            .confidence(calculateConfidence(aiResponse, relevantProfessors))
                            .processingTimeMs(processingTime)
                            .model(getActiveModel())
                            .build());

        } catch (Exception e) {
            log.error("❌ Error processing AI question: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * RAG Step 1: Retrieve relevant professors based on question keywords.
     */
    private List<ProfessorInfo> retrieveRelevantProfessors(AIRequest request) {
        // Extract keywords from question
        List<String> keywords = extractKeywords(request.getQuestion());
        log.debug("🔍 Extracted keywords: {}", keywords);

        // If specific professor mentioned, prioritize
        if (request.getProfessorName() != null && !request.getProfessorName().isBlank()) {
            try {
                ProfessorInfo prof = mcpService.getProfessorInfo(request.getProfessorName());
                return List.of(prof);
            } catch (Exception e) {
                log.warn("⚠️ Professor '{}' not found, using keyword search", request.getProfessorName());
            }
        }

        // Search by keywords
        List<ProfessorInfo> allResults = keywords.stream()
                .flatMap(keyword -> mcpService.searchByResearchArea(keyword).stream())
                .distinct()
                .limit(4) // Limit to 4 professors to fit in context window
                .toList();

        // If no results, return all professors (small dataset of 4)
        if (allResults.isEmpty()) {
            log.debug("📋 No keyword matches, returning all professors");
            return mcpService.getAllProfessorNames().stream()
                    .map(name -> {
                        try {
                            return mcpService.getProfessorInfo(name);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(prof -> prof != null)
                    .toList();
        }

        return allResults;
    }

    /**
     * Extract keywords from question for search.
     */
    private List<String> extractKeywords(String question) {
        // Common research area keywords (Portuguese and English)
        String[] researchKeywords = {
                "IA", "AI", "inteligência artificial", "artificial intelligence",
                "cloud", "nuvem", "computação em nuvem", "cloud computing",
                "distribuído", "distribuída", "distributed", "sistemas distribuídos",
                "machine learning", "aprendizado de máquina",
                "IoT", "internet das coisas", "internet of things",
                "cidades inteligentes", "smart cities",
                "software", "engenharia de software",
                "teste", "testing", "qualidade",
                "autoscaling", "auto-escalonamento",
                "web services", "microserviços"
        };

        String lowerQuestion = question.toLowerCase();

        return Arrays.stream(researchKeywords)
                .filter(keyword -> lowerQuestion.contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * RAG Step 2: Build enriched context with professor data.
     */
    private String buildEnrichedContext(List<ProfessorInfo> professors, AIRequest request) {
        if (professors.isEmpty()) {
            return "Nenhum professor específico encontrado. Use as ferramentas disponíveis para buscar.";
        }

        StringBuilder context = new StringBuilder();
        context.append("Professores disponíveis:\n\n");

        for (ProfessorInfo prof : professors) {
            context.append(String.format(
                    "**%s**\n" +
                            "- Departamento: %s\n" +
                            "- Email: %s\n" +
                            "- Telefone: %s\n" +
                            "- Gabinete: %s\n" +
                            "- Áreas de pesquisa: %s\n\n",
                    prof.getName(),
                    prof.getDepartment(),
                    prof.getEmail(),
                    prof.getPhone() != null ? prof.getPhone() : "Não informado",
                    prof.getOffice() != null ? prof.getOffice() : "Não informado",
                    extractResearchAreas(prof.getContent())));
        }

        // Add custom context if provided
        if (request.getContext() != null && !request.getContext().isBlank()) {
            context.append("\nContexto adicional:\n").append(request.getContext()).append("\n");
        }

        return context.toString();
    }

    /**
     * Extract research areas from professor content (markdown).
     */
    private String extractResearchAreas(String content) {
        if (content == null)
            return "Não especificado";

        // Look for YAML frontmatter areas_principais
        Pattern pattern = Pattern.compile("areas_principais:\\s*\\n((?:\\s*-\\s*.+\\n)+)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1)
                    .replaceAll("\\s*-\\s*", ", ")
                    .replaceAll("\\n", "")
                    .trim();
        }

        return "Veja perfil completo";
    }

    /**
     * Build system prompt for LLM.
     */
    private String buildSystemPrompt(String enrichedContext) {
        return String.format("""
                Você é um assistente inteligente do IMD/BIOME (Instituto Metrópole Digital).
                Sua função é ajudar alunos e visitantes a encontrar informações sobre professores,
                áreas de pesquisa, contato, e horários de atendimento.


                IMPORTANTE: Use apenas caracteres ASCII simples em suas respostas.
                Evite acentos e caracteres especiais (a, e, c, etc).
                Use apenas letras sem acento (a-z, A-Z), numeros (0-9) e pontuacao basica (. , ! ?).

                Seu objetivo e fornecer informacoes precisas sobre professores, cursos e pesquisas.

                CONTEXTO DISPONIVEL:
                %s

                FERRAMENTAS DISPONIVEIS:
                - professorInfo(name): Busca informacoes detalhadas de um professor
                - listProfessors(): Lista todos os professores disponiveis
                - searchByResearchArea(area): Busca professores por area de pesquisa

                INSTRUCOES:
                1. Use as ferramentas quando necessario para obter informacoes precisas
                2. Seja conciso e objetivo nas respostas
                3. Cite suas fontes quando usar informacoes do contexto
                4. Se nao souber algo, admita honestamente
                5. SEMPRE responda usando apenas caracteres ASCII simples
                """, enrichedContext);
    }

    /**
     * Build user prompt.
     */
    private String buildUserPrompt(AIRequest request) {
        return request.getQuestion();
    }

    /**
     * Extract sources from professor list.
     */
    private List<String> extractSources(List<ProfessorInfo> professors) {
        if (professors.isEmpty()) {
            return List.of("Base de Dados IMD/BIOME");
        }

        return professors.stream()
                .map(ProfessorInfo::getName)
                .collect(Collectors.toList());
    }

    /**
     * Calculate confidence based on response quality and context.
     */
    private Double calculateConfidence(String response, List<ProfessorInfo> context) {
        // Simple heuristic-based confidence calculation
        double confidence = 0.5; // Base confidence

        // Higher confidence if we have context
        if (!context.isEmpty()) {
            confidence += 0.2;
        }

        // Higher confidence for longer, detailed responses
        if (response.length() > 100) {
            confidence += 0.15;
        }

        // Higher confidence if response contains specific information
        if (response.contains("@") || response.contains("Email:")) {
            confidence += 0.1;
        }

        if (response.contains("Telefone:") || response.contains("ramal")) {
            confidence += 0.05;
        }

        return Math.min(confidence, 1.0);
    }

    /**
     * Fallback method when AI service fails (Circuit Breaker open or timeout).
     */
    private CompletableFuture<AIResponse> fallbackProcessQuestion(
            AIRequest request, Exception ex) {

        log.error("🔴 Fallback triggered for question: '{}'. Error: {}",
                request.getQuestion(), ex.getMessage());

        String fallbackMessage = """
                Desculpe, o serviço de IA está temporariamente indisponível devido a um problema técnico.

                Por favor, tente novamente em alguns instantes.

                Se precisar de ajuda imediata, você pode:
                - Consultar a lista de professores diretamente: GET /api/ai/professors
                - Buscar por área de pesquisa: GET /api/ai/professors/search?keyword=sua_area
                - Contatar a secretaria do IMD:
                  📧 Email: secretaria@imd.ufrn.br
                  ☎️ Telefone: +55 84 3342-2210

                Nossos sistemas estão sendo monitorados e o problema será resolvido em breve.
                """;

        return CompletableFuture.completedFuture(
                AIResponse.builder()
                        .answer(fallbackMessage)
                        .sources(List.of("Fallback Response"))
                        .confidence(0.0)
                        .processingTimeMs(0L)
                        .model("fallback")
                        .build());
    }

    /**
     * Create mock response when AI is disabled.
     */
    private AIResponse createMockResponse(AIRequest request) {
        return AIResponse.builder()
                .answer("AI Service está desabilitado. Para ativar, configure spring.ai.enabled=true e adicione API key.")
                .sources(List.of("Mock Response"))
                .confidence(0.0)
                .processingTimeMs(0L)
                .model("mock")
                .build();
    }
}
