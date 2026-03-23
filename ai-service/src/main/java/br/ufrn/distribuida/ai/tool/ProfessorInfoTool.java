package br.ufrn.distribuida.ai.tool;

import br.ufrn.distribuida.ai.dto.ProfessorInfo;
import br.ufrn.distribuida.ai.service.MCPInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * Spring AI Tool for retrieving professor information.
 * 
 * This tool allows the LLM to fetch comprehensive information about a professor
 * by name or ID. The LLM will automatically call this function when it needs
 * specific professor details.
 * 
 * Uses Spring AI Function pattern with @Bean (Spring AI 1.0.0-M4+).
 * 
 * @since Sprint 6
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ProfessorInfoTool {
    
    private final MCPInternalService mcpService;
    
    @Bean
    @Description("Retrieves comprehensive information about a professor including " +
                 "research areas, courses taught, contact details (email, phone, office), " +
                 "biography, and academic background. Use this when you need detailed " +
                 "information about a specific professor. You can search by name or ID.")
    public Function<Request, ProfessorInfo> professorInfo() {
        return request -> {
            log.info("🔧 Tool called: getProfessorInfo for '{}'", request.professorIdentifier);
            
            try {
                ProfessorInfo info = mcpService.getProfessorInfo(request.professorIdentifier);
                log.debug("✅ Tool returned professor: {}", info.getName());
                return info;
            } catch (Exception e) {
                log.error("❌ Tool failed to get professor info: {}", e.getMessage());
                throw e;
            }
        };
    }
    
    /**
     * Request record for professor information lookup.
     * 
     * @param professorIdentifier The name or ID of the professor to look up
     */
    public record Request(
        @Description("The name or ID of the professor to look up. " +
                     "Examples: 'Andre Solino', 'Nelio Cacho', 'professor_andre_luiz_da_silva_solino'")
        String professorIdentifier
    ) {}
}
