package br.ufrn.distribuida.ai.tool;

import br.ufrn.distribuida.ai.dto.ProfessorInfo;
import br.ufrn.distribuida.ai.service.MCPInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

/**
 * Spring AI Tool for searching professors by research area.
 * 
 * This tool enables the LLM to find professors who work in specific research areas.
 * It searches through professor profiles, research areas, and content to find matches.
 * 
 * @since Sprint 6
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SearchByResearchAreaTool {
    
    private final MCPInternalService mcpService;
    
    @Bean
    @Description("Searches for professors by research area keyword. Use this when you need to find " +
                 "professors who work on specific topics like 'AI', 'Machine Learning', 'Cloud Computing', " +
                 "'Distributed Systems', 'Software Engineering', etc. Returns detailed information about " +
                 "all matching professors.")
    public Function<Request, List<ProfessorInfo>> searchByResearchArea() {
        return request -> {
            log.info("🔧 Tool called: searchByResearchArea for '{}'", request.researchArea);
            
            List<ProfessorInfo> results = mcpService.searchByResearchArea(request.researchArea);
            log.debug("✅ Tool found {} professors for area '{}'", results.size(), request.researchArea);
            
            return results;
        };
    }
    
    /**
     * Request record for research area search.
     * 
     * @param researchArea The research area keyword to search for
     */
    public record Request(
        @Description("Research area keyword to search for. " +
                     "Examples: 'AI', 'Inteligência Artificial', 'Cloud', 'Nuvem', " +
                     "'Sistemas Distribuídos', 'Machine Learning', 'IoT', 'Cidades Inteligentes'")
        String researchArea
    ) {}
}
