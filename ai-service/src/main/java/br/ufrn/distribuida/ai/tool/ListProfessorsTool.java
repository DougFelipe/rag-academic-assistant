package br.ufrn.distribuida.ai.tool;

import br.ufrn.distribuida.ai.service.MCPInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

/**
 * Spring AI Tool for listing all available professors.
 * 
 * This tool provides a quick list of all professor names in the system.
 * Useful when the LLM needs to know what professors are available or
 * when answering questions like "Who are the professors?" or "List all professors".
 * 
 * @since Sprint 6
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ListProfessorsTool {
    
    private final MCPInternalService mcpService;
    
    @Bean
    @Description("Lists the names of all professors available in the IMD/BIOME system. " +
                 "Use this when you need to show all available professors or when the user " +
                 "asks questions like 'Who are the professors?' or 'List all professors'.")
    public Function<Request, List<String>> listProfessors() {
        return request -> {
            log.info("🔧 Tool called: listAllProfessors");
            
            List<String> professors = mcpService.getAllProfessorNames();
            log.debug("✅ Tool returned {} professors", professors.size());
            
            return professors;
        };
    }
    
    /**
     * Request record for listing professors.
     * This tool doesn't require any parameters.
     */
    public record Request() {}
}
